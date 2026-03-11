package com.dev.service;

import com.dev.util.ConfigManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class AIDirectOrganizer {
    
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();
    
    private static final Gson gson = new Gson();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    public static void organizeWithAI(Path rootFolder) throws IOException {
        String provider = ConfigManager.get("AI_PROVIDER", "LOCAL");
        
        if ("LOCAL".equals(provider)) {
            RecursiveIntelligentOrganizer.organizeRecursively(rootFolder);
            return;
        }
        
        List<FileEntry> allFiles = scanAllFiles(rootFolder);
        if (allFiles.isEmpty()) return;
        
        Map<String, List<FileEntry>> organization = askAIToOrganize(allFiles, provider);
        executeOrganization(rootFolder, organization);
    }
    
    private static List<FileEntry> scanAllFiles(Path rootFolder) throws IOException {
        try (Stream<Path> stream = Files.walk(rootFolder)) {
            return stream.filter(Files::isRegularFile)
                  .filter(p -> !p.toString().contains("FileOrganizer.jar"))
                  .filter(p -> !isInOrganizedFolder(p))
                  .map(path -> new FileEntry(path, rootFolder.relativize(path)))
                  .toList();
        }
    }
    
    private static boolean isInOrganizedFolder(Path file) {
        Path parent = file.getParent();
        if (parent == null) return false;
        
        String parentName = parent.getFileName().toString();
        return parentName.matches("(Documentos|Imagens|Videos|Audios|Scripts|Logs|Wireshark|Projetos|Design|Programacao).*");
    }
    
    private static Map<String, List<FileEntry>> askAIToOrganize(List<FileEntry> files, String provider) throws IOException {
        Map<String, List<FileEntry>> result = new HashMap<>();
        int batchSize = 50;
        
        for (int i = 0; i < files.size(); i += batchSize) {
            int end = Math.min(i + batchSize, files.size());
            List<FileEntry> batch = files.subList(i, end);
            
            String prompt = buildOrganizationPrompt(batch);
            String response = callAI(prompt, provider);
            
            if (response != null) {
                parseAIResponse(response, batch, result);
            }
            
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
        }
        
        return result;
    }
    
    private static String buildOrganizationPrompt(List<FileEntry> files) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Voce e um especialista em organizacao de arquivos. Analise e categorize os arquivos abaixo.\n\n");
        prompt.append("CATEGORIAS PRINCIPAIS:\n");
        prompt.append("- Documentos (PDF, DOC, DOCX, TXT, CSV, XLS, XLSX, PPT, PPTX)\n");
        prompt.append("- Imagens (JPG, PNG, GIF, BMP, SVG, WEBP, PSD, AI)\n");
        prompt.append("- Videos (MP4, AVI, MKV, MOV, WMV, FLV)\n");
        prompt.append("- Audios (MP3, WAV, FLAC, AAC, OGG, M4A)\n");
        prompt.append("- Scripts (SH, BAT, PS1, PY, JS, RB)\n");
        prompt.append("- Logs (LOG, TXT com 'log' no nome)\n");
        prompt.append("- Wireshark (PCAP, PCAPNG, CAP)\n");
        prompt.append("- Codigo (JAVA, C, CPP, H, CS, GO, RS, TS)\n");
        prompt.append("- Web (HTML, CSS, JS, PHP, JSON, XML)\n");
        prompt.append("- Compactados (ZIP, RAR, 7Z, TAR, GZ)\n");
        prompt.append("- Executaveis (EXE, MSI, DLL, SO, APP)\n\n");
        
        prompt.append("REGRAS:\n");
        prompt.append("1. Use EXATAMENTE os nomes das categorias acima\n");
        prompt.append("2. NAO use emojis\n");
        prompt.append("3. Analise a extensao e o contexto do nome\n\n");
        
        prompt.append("ARQUIVOS:\n");
        for (int i = 0; i < files.size(); i++) {
            prompt.append(String.format("%d. %s\n", i + 1, files.get(i).relativePath));
        }
        
        prompt.append("\nRESPONDA APENAS com JSON no formato:\n");
        prompt.append("{\"1\": \"Categoria\", \"2\": \"Categoria\", ...}");
        
        return prompt.toString();
    }
    
    private static String callAI(String prompt, String provider) throws IOException {
        return switch (provider) {
            case "OPENAI" -> callOpenAI(prompt);
            case "GEMINI" -> callGemini(prompt);
            case "CUSTOM" -> callCustom(prompt);
            default -> throw new IOException("Provedor nao configurado");
        };
    }
    
    private static String callOpenAI(String prompt) throws IOException {
        String apiKey = ConfigManager.get("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IOException("OpenAI API Key nao configurada");
        }
        
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "gpt-3.5-turbo");
        
        JsonArray messages = new JsonArray();
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);
        messages.add(message);
        
        requestBody.add("messages", messages);
        requestBody.addProperty("temperature", 0.3);
        requestBody.addProperty("max_tokens", 2000);
        
        RequestBody body = RequestBody.create(gson.toJson(requestBody), JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("OpenAI API erro: " + response.code() + " - " + response.message());
            }
            
            JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
            return json.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();
        }
    }
    
    private static String callGemini(String prompt) throws IOException {
        String apiKey = ConfigManager.get("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IOException("Gemini API Key nao configurada");
        }
        
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
        
        RequestBody body = RequestBody.create(gson.toJson(requestBody), JSON);
        Request request = new Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + apiKey)
                .post(body)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Gemini API erro: " + response.code() + " - " + response.message());
            }
            
            JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
            return json.getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();
        }
    }
    
    private static String callCustom(String prompt) throws IOException {
        String endpoint = ConfigManager.get("CUSTOM_ENDPOINT");
        String apiKey = ConfigManager.get("CUSTOM_API_KEY", "");
        
        if (endpoint == null || endpoint.isEmpty()) {
            throw new IOException("Endpoint customizado nao configurado");
        }
        
        JsonObject requestBody = new JsonObject();
        
        JsonArray messages = new JsonArray();
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);
        messages.add(message);
        
        requestBody.add("messages", messages);
        requestBody.addProperty("model", "gpt-3.5-turbo");
        
        RequestBody body = RequestBody.create(gson.toJson(requestBody), JSON);
        Request.Builder requestBuilder = new Request.Builder()
                .url(endpoint)
                .post(body);
        
        if (!apiKey.isEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer " + apiKey);
        }
        
        try (Response response = client.newCall(requestBuilder.build()).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("API error: " + response.code());
            }
            
            String responseBody = response.body().string();
            JsonObject json = gson.fromJson(responseBody, JsonObject.class);
            
            if (json.has("choices")) {
                return json.getAsJsonArray("choices")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("message")
                        .get("content").getAsString();
            }
            
            return responseBody;
        }
    }
    
    private static void parseAIResponse(String response, List<FileEntry> files, Map<String, List<FileEntry>> result) {
        response = response.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
        
        JsonObject json = gson.fromJson(response, JsonObject.class);
        
        for (int i = 0; i < files.size(); i++) {
            String key = String.valueOf(i + 1);
            String folderName = json.has(key) ? json.get(key).getAsString().trim() : "Outros";
            result.computeIfAbsent(folderName, k -> new ArrayList<>()).add(files.get(i));
        }
    }
    
    private static void executeOrganization(Path rootFolder, Map<String, List<FileEntry>> organization) throws IOException {
        for (Map.Entry<String, List<FileEntry>> entry : organization.entrySet()) {
            String folderName = sanitizeFolderName(entry.getKey());
            List<FileEntry> files = entry.getValue();
            
            if (files.isEmpty()) continue;
            
            Path targetFolder = rootFolder.resolve(folderName);
            Files.createDirectories(targetFolder);
            
            for (FileEntry file : files) {
                Path targetPath = resolveTargetPath(targetFolder, file.path);
                if (!file.path.getParent().equals(targetFolder)) {
                    Files.move(file.path, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }
    
    private static Path resolveTargetPath(Path targetFolder, Path sourcePath) throws IOException {
        Path targetPath = targetFolder.resolve(sourcePath.getFileName());
        
        if (!Files.exists(targetPath)) return targetPath;
        
        String name = sourcePath.getFileName().toString();
        int dotIndex = name.lastIndexOf('.');
        
        int counter = 1;
        while (Files.exists(targetPath)) {
            if (dotIndex > 0) {
                String baseName = name.substring(0, dotIndex);
                String ext = name.substring(dotIndex);
                targetPath = targetFolder.resolve(baseName + "_" + counter + ext);
            } else {
                targetPath = targetFolder.resolve(name + "_" + counter);
            }
            counter++;
        }
        
        return targetPath;
    }
    
    private static void organizeLocally(Path rootFolder) {
        RecursiveIntelligentOrganizer.organizeRecursively(rootFolder);
    }
    
    private static String sanitizeFolderName(String name) {
        return name.replaceAll("[<>:\"/\\\\|?*]", "_").trim();
    }
    
    static class FileEntry {
        Path path;
        Path relativePath;
        String extension;
        
        FileEntry(Path path, Path relativePath) {
            this.path = path;
            this.relativePath = relativePath;
            
            String name = path.getFileName().toString();
            int dotIndex = name.lastIndexOf('.');
            this.extension = dotIndex > 0 ? name.substring(dotIndex + 1) : "";
        }
        
        String getExtension() {
            return extension.isEmpty() ? "SEM_EXTENSAO" : extension;
        }
    }
}
