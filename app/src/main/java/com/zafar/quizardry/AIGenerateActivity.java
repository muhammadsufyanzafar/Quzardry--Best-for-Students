package com.zafar.quizardry;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AIGenerateActivity extends AppCompatActivity {
    private static final String TAG = "AIGenerateActivity";

    private EditText etTopic, etCount;
    private Spinner spContentType;
    private Button btnGenerate, btnCancel;

    private AppDatabase db;
    private final OkHttpClient client = new OkHttpClient();

    private static final String OPENROUTER_ENDPOINT = "https://openrouter.ai/api/v1/chat/completions";
    private static final String MODEL = "google/gemini-2.0-flash-exp:free";

    // Rate-limiting and retry handling
    private static final int COOLDOWN_MS = 60000;        // 60s cooldown after a 429
    private static final int MAX_BACKOFF_ATTEMPTS = 2;    // retry 2 times on 429
    private static final int INITIAL_BACKOFF_MS = 15000;  // 15s then 30s
    private int backoffAttempt = 0;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_generate);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("AI Content Generator");

        db = AppDatabase.getInstance(this);

        etTopic       = findViewById(R.id.etTopic);
        etCount       = findViewById(R.id.etCount);
        spContentType = findViewById(R.id.spContentType);
        btnGenerate   = findViewById(R.id.btnGenerate);
        btnCancel     = findViewById(R.id.btnCancel);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[] { "Flashcards", "Quizzes" }
        );
        spContentType.setAdapter(adapter);

        btnGenerate.setOnClickListener(v -> {
            backoffAttempt = 0; // reset for a fresh series
            onGenerate();
        });
        btnCancel.setOnClickListener(v -> finish());
    }

    private void onGenerate() {
        String topic    = etTopic.getText().toString().trim();
        String type     = spContentType.getSelectedItem().toString();
        String countStr = etCount.getText().toString().trim();

        if (topic.isEmpty()) {
            toast("Please enter a topic");
            return;
        }

        int count = 10;
        try { count = Integer.parseInt(countStr); } catch (Exception ignored) {}

        String apiKey = resolveApiKey();
        if (apiKey.isEmpty()) {
            toast("Missing API key! Add it in secrets.xml or manifest meta-data");
            return;
        }

        String prompt = buildPrompt(topic, type, count);
        disableButton("Generating...");
        sendRequest(apiKey, prompt, type);
    }

    private void sendRequest(String apiKey, String prompt, String type) {
        JsonObject req = new JsonObject();
        req.addProperty("model", MODEL);

        JsonArray messages = new JsonArray();

        JsonObject sys = new JsonObject();
        sys.addProperty("role", "system");
        sys.addProperty("content", "You will respond with STRICT JSON only inside triple backticks. No commentary.");
        messages.add(sys);

        JsonObject user = new JsonObject();
        user.addProperty("role", "user");
        user.addProperty("content", prompt);
        messages.add(user);

        req.add("messages", messages);
        req.addProperty("temperature", 0.7);

        RequestBody body = RequestBody.create(req.toString(), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(OPENROUTER_ENDPOINT)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> {
                    enableButton();
                    toast("Network error: " + e.getMessage());
                });
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String raw = response.body() != null ? response.body().string() : "";

                // Handle rate limit 429
                if (response.code() == 429) {
                    if (backoffAttempt < MAX_BACKOFF_ATTEMPTS) {
                        int delay = INITIAL_BACKOFF_MS * (int) Math.pow(2, backoffAttempt); // 15s, then 30s
                        backoffAttempt++;
                        mainHandler.post(() -> {
                            toast("Rate limit reached. Retrying in " + (delay / 1000) + "s...");
                            disableButton("Retrying in " + (delay / 1000) + "s...");
                        });
                        mainHandler.postDelayed(() -> {
                            String topic    = etTopic.getText().toString().trim();
                            String typeSel  = spContentType.getSelectedItem().toString();
                            String countStr = etCount.getText().toString().trim();
                            int count = 10;
                            try { count = Integer.parseInt(countStr); } catch (Exception ignored) {}
                            String promptRetry = buildPrompt(topic, typeSel, count);
                            sendRequest(apiKey, promptRetry, typeSel);
                        }, delay);
                    } else {
                        mainHandler.post(() -> {
                            toast("Rate limit still active. Please wait a minute and try again.");
                            startCooldown();
                        });
                    }
                    return;
                }

                if (!response.isSuccessful()) {
                    mainHandler.post(() -> {
                        enableButton();
                        toast("API error: " + response.code());
                    });
                    return;
                }

                try {
                    JsonObject root = JsonParser.parseString(raw).getAsJsonObject();
                    JsonArray choices = root.getAsJsonArray("choices");
                    if (choices == null || choices.size() == 0) throw new IllegalStateException("No choices from model");
                    JsonObject msg = choices.get(0).getAsJsonObject().getAsJsonObject("message");
                    String content = msg.get("content").getAsString();
                    String jsonOnly = extractJson(content);
                    Log.d(TAG, "AI JSON:\n" + jsonOnly);

                    if (type.equals("Flashcards")) {
                        List<Flashcard> items = parseFlashcards(jsonOnly);
                        saveFlashcards(items);
                    } else {
                        List<QuizQuestion> qs = parseQuiz(jsonOnly);
                        saveQuiz(qs);
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "Parse failed", ex);
                    mainHandler.post(() -> {
                        enableButton();
                        toast("Parse failed. Try a simpler topic or fewer items.");
                    });
                }
            }
        });
    }

    private void startCooldown() {
        btnGenerate.setEnabled(false);
        btnGenerate.setText("Please wait...");
        btnGenerate.postDelayed(this::enableButton, COOLDOWN_MS);
    }

    private void disableButton(String label) {
        btnGenerate.setEnabled(false);
        btnGenerate.setText(label);
    }

    private void enableButton() {
        btnGenerate.setEnabled(true);
        btnGenerate.setText("Generate Content");
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    // Strong prompt for better answers; forbids answer==question
    private String buildPrompt(String topic, String type, int count) {
        if ("Flashcards".equals(type)) {
            return "TASK: Create " + count + " flashcards about \"" + topic + "\".\n" +
                    "Respond ONLY with valid JSON inside triple backticks. No extra text.\n" +
                    "Rules:\n" +
                    "- Each item has `question` and `answer` (answer must be the correct response).\n" +
                    "- `answer` MUST NOT repeat `question`.\n" +
                    "Schema:\n" +
                    "```json\n" +
                    "{ \"flashcards\": [ { \"question\": \"string\", \"answer\": \"string\" } ] }\n" +
                    "```";
        } else {
            return "TASK: Create " + count + " multiple-choice questions about \"" + topic + "\".\n" +
                    "Respond ONLY with valid JSON inside triple backticks. No extra text.\n" +
                    "Schema:\n" +
                    "```json\n" +
                    "{ \"questions\": [ { \"question\": \"string\", \"options\": [\"A\",\"B\",\"C\",\"D\"], \"correctIndex\": 0 } ] }\n" +
                    "```";
        }
    }

    // Extract JSON from LLM output that may be fenced in triple backticks
    private String extractJson(String content) {
        int fence = content.indexOf("```");
        if (fence != -1) {
            int start = content.indexOf("{", fence);
            int end   = content.lastIndexOf("```");
            if (start != -1 && end != -1 && end > start) {
                return content.substring(start, end).trim();
            }
        }
        int first = content.indexOf("{");
        int last  = content.lastIndexOf("}");
        if (first != -1 && last != -1 && last > first) {
            return content.substring(first, last + 1).trim();
        }
        return content.trim();
    }

    // Parse flashcards JSON into Room entities; skip invalid Q==A
    private List<Flashcard> parseFlashcards(String json) {
        List<Flashcard> list = new ArrayList<>();
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        JsonArray arr = obj.getAsJsonArray("flashcards");
        if (arr == null) return list;
        for (JsonElement el : arr) {
            JsonObject f = el.getAsJsonObject();
            String q = safeStr(f, "question").trim();
            String a = safeStr(f, "answer").trim();
            if (q.isEmpty() || a.isEmpty() || q.equalsIgnoreCase(a)) {
                Log.w(TAG, "Skipping invalid flashcard (empty or question==answer): " + q);
                continue;
            }
            list.add(new Flashcard(q, a));
        }
        return list;
    }

    // Parse quiz JSON into Room entities
    private List<QuizQuestion> parseQuiz(String json) {
        List<QuizQuestion> list = new ArrayList<>();
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        JsonArray arr = obj.getAsJsonArray("questions");
        if (arr == null) return list;
        for (JsonElement el : arr) {
            JsonObject q = el.getAsJsonObject();
            String question = safeStr(q, "question");
            JsonArray opts = q.getAsJsonArray("options");
            int idx = q.has("correctIndex") && !q.get("correctIndex").isJsonNull()
                    ? q.get("correctIndex").getAsInt() : 0;
            if (question.isEmpty() || opts == null || opts.size() != 4) continue;

            String A = opts.get(0).getAsString();
            String B = opts.get(1).getAsString();
            String C = opts.get(2).getAsString();
            String D = opts.get(3).getAsString();
            if (idx < 0 || idx > 3) idx = 0;

            list.add(new QuizQuestion(question, A, B, C, D, idx));
        }
        return list;
    }

    private String safeStr(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : "";
    }

    private void saveFlashcards(List<Flashcard> items) {
        new Thread(() -> {
            for (Flashcard fc : items) {
                db.flashcardDao().insert(fc);
            }
            mainHandler.post(() -> {
                enableButton();
                toast("Added " + items.size() + " flashcards");
                finish();
            });
        }).start();
    }

    private void saveQuiz(List<QuizQuestion> qs) {
        new Thread(() -> {
            for (QuizQuestion q : qs) {
                db.quizDao().insert(q);
            }
            mainHandler.post(() -> {
                enableButton();
                toast("Added " + qs.size() + " quiz questions");
                finish();
            });
        }).start();
    }

    // Fallback chain: BuildConfig → resource string → manifest meta-data
    private String resolveApiKey() {
        // 1) BuildConfig
        try {
            String k = com.zafar.quizardry.BuildConfig.OPENROUTER_API_KEY;
            if (k != null && !k.isEmpty()) return k;
        } catch (Throwable ignored) { }

        // 2) Resource (secrets.xml)
        try {
            String r = getString(R.string.openrouter_api_key);
            if (r != null && !r.isEmpty()) return r;
        } catch (Exception ignored) { }

        // 3) Manifest meta-data
        try {
            ApplicationInfo ai = getPackageManager()
                    .getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            android.os.Bundle md = ai.metaData;
            if (md != null) {
                String m = md.getString("OPENROUTER_API_KEY", "");
                if (m != null && !m.isEmpty()) return m;
            }
        } catch (PackageManager.NameNotFoundException ignored) { }

        return "";
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
