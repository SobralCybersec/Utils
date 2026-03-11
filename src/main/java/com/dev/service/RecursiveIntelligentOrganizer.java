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

public final class RecursiveIntelligentOrganizer {
    
    private static final int TIMEOUT_SECONDS = 30;
    private static final int MAX_FILES_PER_ANALYSIS = 20;
    private static final int MIN_FILES_FOR_AI = 3;
    private static final int MAX_TOKENS = 100;
    private static final double TEMPERATURE = 0.3;
    
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build();
    
    private static final Gson gson = new Gson();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private static final Map<String, List<String>> PROGRAM_PATTERNS = initializePatterns();
    
    private RecursiveIntelligentOrganizer() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    private static Map<String, List<String>> initializePatterns() {
        Map<String, List<String>> patterns = new HashMap<>();
        patterns.put("Visual Studio Code", Arrays.asList(".vscode", "*.code-workspace"));
        patterns.put("IntelliJ IDEA", Arrays.asList(".idea", "*.iml"));
        patterns.put("Eclipse", Arrays.asList(".project", ".classpath", ".settings"));
        patterns.put("Git", Arrays.asList(".git", ".gitignore", ".gitattributes"));
        patterns.put("Node.js", Arrays.asList("package.json", "node_modules", "yarn.lock"));
        patterns.put("Python", Arrays.asList("requirements.txt", "setup.py", "*.pyc", "__pycache__"));
        patterns.put("Java Maven", Arrays.asList("pom.xml", "target"));
        patterns.put("Java Gradle", Arrays.asList("build.gradle", "gradlew"));
        patterns.put("Docker", Arrays.asList("Dockerfile", "docker-compose.yml", ".dockerignore"));
        patterns.put("Unity", Arrays.asList("Assets", "ProjectSettings", "*.unity"));
        patterns.put("Unreal Engine", Arrays.asList("*.uproject", "Content", "Source"));
        patterns.put("Android Studio", Arrays.asList("AndroidManifest.xml", "app/build.gradle"));
        patterns.put("Photoshop", Arrays.asList("*.psd", "*.psb"));
        patterns.put("Illustrator", Arrays.asList("*.ai"));
        patterns.put("Premiere", Arrays.asList("*.prproj"));
        patterns.put("After Effects", Arrays.asList("*.aep"));
        patterns.put("Blender", Arrays.asList("*.blend", "*.blend1"));
        patterns.put("AutoCAD", Arrays.asList("*.dwg", "*.dxf"));
        return Collections.unmodifiableMap(patterns);
    }
    
    public static void organizeRecursively(Path rootFolder) {
        try {
            Map<Path, FileInfo> allFiles = scanRecursively(rootFolder);
            Map<String, List<Path>> programGroups = detectPrograms(allFiles);
            
            if (!ConfigManager.get("AI_PROVIDER", "LOCAL").equals("LOCAL")) {
                programGroups = enhanceWithAI(programGroups, allFiles);
            }
            
            organizeByPrograms(rootFolder, programGroups);
        } catch (Exception e) {
            System.err.println("Failed to organize recursively: " + e.getMessage());
        }
    }
    
    private static Map<Path, FileInfo> scanRecursively(Path rootFolder) throws IOException {
        Map<Path, FileInfo> files = new HashMap<>();
        
        try (Stream<Path> stream = Files.walk(rootFolder)) {
            stream.filter(Files::isRegularFile)
                  .filter(p -> !p.toString().contains("FileOrganizer.jar"))
                  .forEach(path -> {
                      try {
                          files.put(path, new FileInfo(path, rootFolder));
                      } catch (IOException e) {
                          System.err.println("Failed to read: " + path);
                      }
                  });
        }
        
        return files;
    }
    
    private static Map<String, List<Path>> detectPrograms(Map<Path, FileInfo> allFiles) {
        Map<String, List<Path>> groups = new HashMap<>();
        Map<Path, String> folderPrograms = new HashMap<>();
        
        for (Map.Entry<Path, FileInfo> entry : allFiles.entrySet()) {
            Path file = entry.getKey();
            FileInfo info = entry.getValue();
            Path folder = file.getParent();
            
            if (folderPrograms.containsKey(folder)) {
                String program = folderPrograms.get(folder);
                groups.computeIfAbsent(program, k -> new ArrayList<>()).add(file);
                continue;
            }
            
            String detectedProgram = detectProgramByFile(file, info);
            if (detectedProgram != null) {
                folderPrograms.put(folder, detectedProgram);
                groups.computeIfAbsent(detectedProgram, k -> new ArrayList<>()).add(file);
            } else {
                String ext = info.extension.toUpperCase();
                if (!ext.isEmpty()) {
                    groups.computeIfAbsent("Arquivos " + ext, k -> new ArrayList<>()).add(file);
                }
            }
        }
        
        return groups;
    }
    
    private static String detectProgramByFile(Path file, FileInfo info) {
        String fileName = file.getFileName().toString().toLowerCase();
        
        for (Map.Entry<String, List<String>> entry : PROGRAM_PATTERNS.entrySet()) {
            for (String pattern : entry.getValue()) {
                if (matchesPattern(fileName, pattern)) {
                    return entry.getKey();
                }
            }
        }
        
        return detectByExtension(info.extension.toLowerCase());
    }
    
    private static boolean matchesPattern(String fileName, String pattern) {
        if (pattern.startsWith("*.")) {
            return fileName.endsWith(pattern.substring(1));
        }
        return fileName.equals(pattern.toLowerCase());
    }
    
    private static String detectByExtension(String extension) {
        return switch (extension) {
            case "psd", "psb" -> "Adobe Photoshop";
            case "ai", "eps" -> "Adobe Illustrator";
            case "prproj" -> "Adobe Premiere";
            case "aep" -> "Adobe After Effects";
            case "blend" -> "Blender";
            case "dwg", "dxf" -> "AutoCAD";
            case "fig" -> "Figma";
            case "sketch" -> "Sketch";
            case "xd" -> "Adobe XD";
            case "unity" -> "Unity";
            case "uproject" -> "Unreal Engine";
            case "sln", "csproj" -> "Visual Studio";
            case "xcodeproj" -> "Xcode";
            case "apk" -> "Android";
            case "ipa" -> "iOS";
            default -> null;
        };
    }
    
    private static Map<String, List<Path>> enhanceWithAI(Map<String, List<Path>> groups, Map<Path, FileInfo> allFiles) {
        try {
            Map<Path, List<Path>> filesByFolder = groupByFolder(allFiles);
            
            for (Map.Entry<Path, List<Path>> entry : filesByFolder.entrySet()) {
                if (entry.getValue().size() < MIN_FILES_FOR_AI) continue;
                
                String aiCategory = analyzeWithAI(entry.getKey(), entry.getValue());
                if (aiCategory != null && !aiCategory.isEmpty()) {
                    groups.computeIfAbsent(aiCategory, k -> new ArrayList<>()).addAll(entry.getValue());
                }
            }
        } catch (Exception e) {
            System.err.println("AI analysis failed: " + e.getMessage());
        }
        
        return groups;
    }
    
    private static Map<Path, List<Path>> groupByFolder(Map<Path, FileInfo> allFiles) {
        Map<Path, List<Path>> filesByFolder = new HashMap<>();
        for (Path file : allFiles.keySet()) {
            Path folder = file.getParent();
            filesByFolder.computeIfAbsent(folder, k -> new ArrayList<>()).add(file);
        }
        return filesByFolder;
    }
    
    private static String analyzeWithAI(Path folder, List<Path> files) {
        try {
            String provider = ConfigManager.get("AI_PROVIDER", "LOCAL");
            if ("LOCAL".equals(provider)) return null;
            
            String prompt = buildAIPrompt(folder, files);
            return callAI(prompt, provider);
        } catch (Exception e) {
            return null;
        }
    }
    
    private static String buildAIPrompt(Path folder, List<Path> files) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analise estes arquivos e identifique o programa/projeto:\n\n");
        prompt.append("Pasta: ").append(folder.getFileName()).append("\n");
        prompt.append("Arquivos:\n");
        
        int count = 0;
        for (Path file : files) {
            if (count++ >= MAX_FILES_PER_ANALYSIS) break;
            prompt.append("- ").append(file.getFileName()).append("\n");
        }
        
        prompt.append("\nResponda APENAS com o nome do programa/projeto (ex: 'Projeto Node.js', 'Fotos Photoshop')");
        return prompt.toString();
    }
    
    private static String callAI(String prompt, String provider) throws IOException {
        return switch (provider) {
            case "OPENAI" -> callOpenAI(prompt, ConfigManager.get("OPENAI_API_KEY"));
            case "GEMINI" -> callGemini(prompt, ConfigManager.get("GEMINI_API_KEY"));
            case "CUSTOM" -> callCustom(prompt, ConfigManager.get("CUSTOM_ENDPOINT"), ConfigManager.get("CUSTOM_API_KEY", ""));
            default -> null;
        };
    }
    
    private static String callOpenAI(String prompt, String apiKey) throws IOException {
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
    
    private static String callGemini(String prompt, String apiKey) throws IOException {
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
    
    private static String callCustom(String prompt, String endpoint, String apiKey) throws IOException {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("prompt", prompt);
        
        Request.Builder requestBuilder = new Request.Builder()
                .url(endpoint)
                .post(RequestBody.create(gson.toJson(requestBody), JSON));
        
        if (!apiKey.isEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer " + apiKey);
        }
        
        return executeRequest(requestBuilder.build(), "custom");
    }
    
    private static String executeRequest(Request request, String provider) throws IOException {
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) return null;
            
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
    
    private static void organizeByPrograms(Path rootFolder, Map<String, List<Path>> groups) {
        for (Map.Entry<String, List<Path>> entry : groups.entrySet()) {
            String programName = sanitizeFolderName(entry.getKey());
            List<Path> files = entry.getValue();
            
            if (files.isEmpty()) continue;
            
            Path targetFolder = rootFolder.resolve(programName);
            
            try {
                Files.createDirectories(targetFolder);
                
                for (Path file : files) {
                    try {
                        Path relativePath = rootFolder.relativize(file);
                        Path targetPath = targetFolder.resolve(relativePath.getFileName());
                        
                        if (file.getParent().equals(targetFolder)) continue;
                        
                        Files.move(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (Exception e) {
                        System.err.println("Failed to move file: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to create folder: " + e.getMessage());
            }
        }
    }
    
    private static String sanitizeFolderName(String name) {
        return name.replaceAll("[<>:\"/\\\\|?*]", "_").trim();
    }
    
    static class FileInfo {
        final Path path;
        final String name;
        final String extension;
        final long size;
        final Path relativePath;
        
        FileInfo(Path path, Path rootFolder) throws IOException {
            this.path = path;
            this.name = path.getFileName().toString();
            this.size = Files.size(path);
            this.relativePath = rootFolder.relativize(path);
            
            int dotIndex = name.lastIndexOf('.');
            this.extension = dotIndex > 0 ? name.substring(dotIndex + 1) : "";
        }
    }
}
