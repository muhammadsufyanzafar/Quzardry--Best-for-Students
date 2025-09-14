package com.zafar.quizardry;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.*;
import okhttp3.*;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_generate);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("AI Content Generator");

        db = AppDatabase.getInstance(this);

        etTopic        = findViewById(R.id.etTopic);
        etCount        = findViewById(R.id.etCount);
        spContentType  = findViewById(R.id.spContentType);
        btnGenerate    = findViewById(R.id.btnGenerate);
        btnCancel      = findViewById(R.id.btnCancel);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[] { "Flashcards", "Quizzes" }
        );
        spContentType.setAdapter(adapter);

        btnGenerate.setOnClickListener(v -> onGenerate());
        btnCancel  .setOnClickListener(v -> finish());
    }

    private void onGenerate() {
        String topic   = etTopic.getText().toString().trim();
        String type    = spContentType.getSelectedItem().toString();
        String countStr= etCount.getText().toString().trim();

        if (topic.isEmpty()) {
            toast("Please enter a topic");
            return;
        }

        int count = 10;
        try {
            count = Integer.parseInt(countStr);
        } catch (Exception ignored) {}

        String prompt = buildPrompt(topic, type, count);
        String apiKey = resolveApiKey();
        if (apiKey.isEmpty()) {
            toast("Missing API key! See secrets.xml or manifest meta-data");
            return;
        }

        btnGenerate.setEnabled(false);
        btnGenerate.setText("Generating...");

        JsonObject req = new JsonObject();
        req.addProperty("model", MODEL);

        JsonArray messages = new JsonArray();
        JsonObject sys = new JsonObject();
        sys.addProperty("role", "system");
        sys.addProperty("content",
                "You will respond with STRICT JSON only inside triple backticks. " +
                        "No commentary.");
        messages.add(sys);

        JsonObject user = new JsonObject();
        user.addProperty("role", "user");
        user.addProperty("content", prompt);
        messages.add(user);

        req.add("messages", messages);
        req.addProperty("temperature", 0.7);

        RequestBody body = RequestBody.create(
                req.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(OPENROUTER_ENDPOINT)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    resetButton();
                    toast("Network error: " + e.getMessage());
                });
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String raw = response.body() != null ? response.body().string() : "";
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> {
                        resetButton();
                        toast("API error: " + response.code());
                    });
                    return;
                }
                try {
                    JsonObject root = JsonParser.parseString(raw).getAsJsonObject();
                    JsonArray choices = root.getAsJsonArray("choices");
                    JsonObject msg = choices.get(0).getAsJsonObject()
                            .getAsJsonObject("message");
                    String content = msg.get("content").getAsString();
                    String jsonOnly = extractJson(content);
                    Log.d(TAG, "JSON only →\n" + jsonOnly);

                    if (type.equals("Flashcards")) {
                        List<Flashcard> items = parseFlashcards(jsonOnly);
                        saveFlashcards(items);
                    } else {
                        List<QuizQuestion> qs = parseQuiz(jsonOnly);
                        saveQuiz(qs);
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "Parse failed", ex);
                    runOnUiThread(() -> {
                        resetButton();
                        toast("Parse failed. See log for details.");
                    });
                }
            }
        });
    }

    // Reset generate button state
    private void resetButton() {
        btnGenerate.setEnabled(true);
        btnGenerate.setText("Generate Content");
    }

    // Simple toast helper
    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    // Build a strict JSON-only prompt
    private String buildPrompt(String topic, String type, int count) {
        if ("Flashcards".equals(type)) {
            return "TASK: Create " + count + " flashcards about \"" + topic + "\".\n" +
                    "RESPOND with ONLY valid JSON inside triple backticks.\n" +
                    "SCHEMA:\n" +
                    "```json\n" +
                    "{ \"flashcards\": [ { \"question\": \"string\", \"answer\": \"string\" } ] }\n" +
                    "```";
        } else {
            return "TASK: Create " + count + " multiple-choice questions about \"" + topic + "\".\n" +
                    "RESPOND with ONLY valid JSON inside triple backticks.\n" +
                    "SCHEMA:\n" +
                    "```json\n" +
                    "{ \"questions\": [ { \"question\": \"string\", \"options\": [\"A\",\"B\",\"C\",\"D\"], \"correctIndex\": 0 } ] }\n" +
                    "```";
        }
    }

    // Extract the JSON block from any extra text or fences
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

    // Parse flashcards JSON into Room entities
    private List<Flashcard> parseFlashcards(String json) {
        List<Flashcard> list = new ArrayList<>();
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        JsonArray arr = obj.getAsJsonArray("flashcards");
        if (arr == null) return list;
        for (JsonElement el : arr) {
            JsonObject f = el.getAsJsonObject();
            String q = safeStr(f, "question");
            String a = safeStr(f, "answer");
            if (!q.isEmpty() && !a.isEmpty()) {
                list.add(new Flashcard(q, a));
            }
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
            int idx = q.has("correctIndex") ? q.get("correctIndex").getAsInt() : 0;
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
            runOnUiThread(() -> {
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
            runOnUiThread(() -> {
                toast("Added " + qs.size() + " quiz questions");
                finish();
            });
        }).start();
    }

    // Fallback chain: BuildConfig → un-versioned resource → manifest meta-data
    private String resolveApiKey() {
        // 1) BuildConfig
        try {
            String k = com.zafar.quizardry.BuildConfig.OPENROUTER_API_KEY;
            if (k != null && !k.isEmpty()) return k;
        } catch (Throwable ignored) { }

        // 2) Resource (secrets.xml, Git-ignored)
        try {
            String r = getString(R.string.openrouter_api_key);
            if (r != null && !r.isEmpty()) return r;
        } catch (Exception ignored) { }

        // 3) Manifest meta-data
        try {
            ApplicationInfo ai = getPackageManager()
                    .getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            Bundle md = ai.metaData;
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
