package com.dev.service;

import com.dev.util.ConfigManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

public final class LLMOrganizerService {
    
    private static final int TIMEOUT_SECONDS = 30;
    private static final int BATCH_SIZE = 20;
    private static final int MAX_TOKENS = 500;
    private static final double TEMPERATURE = 0.3;
    
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build();
    
    private static final Gson gson = new Gson();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private static final String[] CATEGORY_NAMES = {
        "Projetos", "Trabalho", "Estudos", "Midia", "Downloads",
        "Backups", "Sistema", "Instaladores", "Documentos", "Outros"
    };
    
    static {
        ConfigManager.loadConfig();
    }
    
    private LLMOrganizerService() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    public static void configure(String provider, String key, String endpoint) {
        Map<String, String> config = new HashMap<>();
        config.put("AI_PROVIDER", provider);
        
        switch (provider) {
            case "OPENAI" -> config.put("OPENAI_API_KEY", key);
            case "GEMINI" -> config.put("GEMINI_API_KEY", key);
            case "CUSTOM" -> {
                config.put("CUSTOM_ENDPOINT", endpoint);
                config.put("CUSTOM_API_KEY", key);
            }
        }
        
        ConfigManager.saveConfig(config);
    }
    
    public static Map<Path, String> organizeWithLLM(List<Path> files) {
        if (files.isEmpty()) {
            return Collections.emptyMap();
        }
        
        String provider = getProvider();
        
        try {
            if ("LOCAL".equals(provider)) {
                return organizeLocally(files);
            }
            
            return processBatches(files);
        } catch (Exception e) {
            System.err.println("AI failed, using local analysis: " + e.getMessage());
            return organizeLocally(files);
        }
    }
    
    private static Map<Path, String> processBatches(List<Path> files) throws IOException {
        Map<Path, String> result = new HashMap<>();
        List<List<Path>> batches = createBatches(files, BATCH_SIZE);
        
        for (List<Path> batch : batches) {
            result.putAll(processBatchWithLLM(batch));
        }
        
        return result;
    }
    
    private static Map<Path, String> organizeLocally(List<Path> files) {
        Map<Path, String> result = new HashMap<>();
        files.forEach(file -> {
            String category = IntelligentAnalyzer.analyzeFile(file);
            String folderName = IntelligentAnalyzer.getIntelligentFolderName(category);
            result.put(file, folderName);
        });
        return result;
    }
    
    private static Map<Path, String> processBatchWithLLM(List<Path> batch) throws IOException {
        String prompt = buildPrompt(batch);
        String response = callLLM(prompt);
        return parseResponse(response, batch);
    }
    
    private static String buildPrompt(List<Path> files) {
        StringBuilder sb = new StringBuilder();
        sb.append("Voce e um assistente de organizacao de arquivos. Analise os nomes e sugira categorias.\n\n");
        sb.append("Categorias:\n");
        sb.append("1. Projetos - codigo, desenvolvimento\n");
        sb.append("2. Trabalho - documentos profissionais\n");
        sb.append("3. Estudos - materiais academicos\n");
        sb.append("4. Midia - fotos, videos, musica\n");
        sb.append("5. Downloads - temporarios\n");
        sb.append("6. Backups - copias\n");
        sb.append("7. Sistema - configs, logs\n");
        sb.append("8. Instaladores - setup\n");
        sb.append("9. Documentos - gerais\n");
        sb.append("10. Outros\n\n");
        sb.append("Arquivos:\n");
        
        for (int i = 0; i < files.size(); i++) {
            sb.append(i + 1).append(". ").append(files.get(i).getFileName()).append("\n");
        }
        
        sb.append("\nResponda apenas com numeros separados por virgula (ex: 1,4,2,9...)");
        return sb.toString();
    }
    
    private static String callLLM(String prompt) throws IOException {
        String provider = getProvider();
        return switch (provider) {
            case "OPENAI" -> callOpenAI(prompt);
            case "GEMINI" -> callGemini(prompt);
            case "CUSTOM" -> callCustom(prompt);
            default -> throw new IOException("Provider not configured");
        };
    }
    
    private static String callOpenAI(String prompt) throws IOException {
        String apiKey = ConfigManager.get("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IOException("API Key not configured");
        }
        
        JsonObject requestBody = buildOpenAIRequest(prompt);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(gson.toJson(requestBody), JSON))
                .build();
        
        return executeRequest(request, "openai");
    }
    
    private static JsonObject buildOpenAIRequest(String prompt) {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "gpt-3.5-turbo");
        requestBody.addProperty("temperature", TEMPERATURE);
        requestBody.addProperty("max_tokens", MAX_TOKENS);
        
        JsonArray messages = new JsonArray();
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);
        messages.add(message);
        requestBody.add("messages", messages);
        
        return requestBody;
    }
    
    private static String callGemini(String prompt) throws IOException {
        String apiKey = ConfigManager.get("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IOException("API Key not configured");
        }
        
        JsonObject requestBody = buildGeminiRequest(prompt);
        Request request = new Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + apiKey)
                .post(RequestBody.create(gson.toJson(requestBody), JSON))
                .build();
        
        return executeRequest(request, "gemini");
    }
    
    private static JsonObject buildGeminiRequest(String prompt) {
        JsonObject requestBody = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();
        
        part.addProperty("text", prompt);
        parts.add(part);
        content.add("parts", parts);
        contents.add(content);
        requestBody.add("contents", contents);
        
        return requestBody;
    }
    
    private static String callCustom(String prompt) throws IOException {
        String customEndpoint = ConfigManager.get("CUSTOM_ENDPOINT");
        String apiKey = ConfigManager.get("CUSTOM_API_KEY", "");
        
        if (customEndpoint == null || customEndpoint.isEmpty()) {
            throw new IOException("Custom endpoint not configured");
        }
        
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("prompt", prompt);
        requestBody.addProperty("model", "gpt-3.5-turbo");
        
        Request.Builder requestBuilder = new Request.Builder()
                .url(customEndpoint)
                .post(RequestBody.create(gson.toJson(requestBody), JSON));
        
        if (!apiKey.isEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer " + apiKey);
        }
        
        return executeRequest(requestBuilder.build(), "custom");
    }
    
    private static String executeRequest(Request request, String provider) throws IOException {
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("API error: " + response.code());
            }
            
            String responseBody = response.body().string();
            return extractContent(responseBody, provider);
        }
    }
    
    private static String extractContent(String responseBody, String provider) {
        JsonObject json = gson.fromJson(responseBody, JsonObject.class);
        
        return switch (provider) {
            case "openai" -> json.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();
            case "gemini" -> json.getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();
            default -> responseBody;
        };
    }
    
    private static Map<Path, String> parseResponse(String response, List<Path> files) {
        Map<Path, String> result = new HashMap<>();
        
        try {
            String[] categories = response.trim().replaceAll("[\n\r]", "").split(",");
            
            for (int i = 0; i < files.size() && i < categories.length; i++) {
                int catIndex = parseCategoryIndex(categories[i]);
                result.put(files.get(i), CATEGORY_NAMES[catIndex]);
            }
        } catch (Exception e) {
            System.err.println("Failed to parse AI response: " + e.getMessage());
        }
        
        files.stream()
             .filter(file -> !result.containsKey(file))
             .forEach(file -> {
                 String category = IntelligentAnalyzer.analyzeFile(file);
                 result.put(file, IntelligentAnalyzer.getIntelligentFolderName(category));
             });
        
        return result;
    }
    
    private static int parseCategoryIndex(String category) {
        try {
            int index = Integer.parseInt(category.trim()) - 1;
            return (index >= 0 && index < CATEGORY_NAMES.length) ? index : CATEGORY_NAMES.length - 1;
        } catch (NumberFormatException e) {
            return CATEGORY_NAMES.length - 1;
        }
    }
    
    private static List<List<Path>> createBatches(List<Path> files, int batchSize) {
        List<List<Path>> batches = new ArrayList<>();
        for (int i = 0; i < files.size(); i += batchSize) {
            batches.add(files.subList(i, Math.min(i + batchSize, files.size())));
        }
        return batches;
    }
    
    public static boolean isConfigured() {
        String provider = getProvider();
        if ("LOCAL".equals(provider)) return true;
        
        return switch (provider) {
            case "OPENAI" -> isKeyConfigured("OPENAI_API_KEY");
            case "GEMINI" -> isKeyConfigured("GEMINI_API_KEY");
            case "CUSTOM" -> isKeyConfigured("CUSTOM_ENDPOINT");
            default -> false;
        };
    }
    
    private static boolean isKeyConfigured(String key) {
        String value = ConfigManager.get(key);
        return value != null && !value.isEmpty();
    }
    
    public static String getProvider() {
        return ConfigManager.get("AI_PROVIDER", "LOCAL");
    }
}
