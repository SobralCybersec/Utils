package com.dev.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class IntelligentAnalyzer {
    
    private static final long MILLIS_PER_DAY = 1000 * 60 * 60 * 24;
    private static final long RECENT_DAYS_THRESHOLD = 7;
    private static final long OLD_DAYS_THRESHOLD = 365;
    private static final long LARGE_FILE_SIZE = 100 * 1024 * 1024;
    private static final long SMALL_FILE_SIZE = 10 * 1024;
    
    private static final Map<String, List<Pattern>> CATEGORIES = initializeCategories();
    private static final Map<String, String> FOLDER_NAMES = initializeFolderNames();
    
    private IntelligentAnalyzer() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    private static Map<String, List<Pattern>> initializeCategories() {
        Map<String, List<Pattern>> categories = new HashMap<>();
        
        categories.put("PROJECTS", Arrays.asList(
            Pattern.compile(".*\\.(java|py|js|ts|cpp|c|go|rs)$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*(project|src|main|app).*", Pattern.CASE_INSENSITIVE)
        ));
        
        categories.put("WORK_DOCS", List.of(
            Pattern.compile(".*(relatorio|report|apresentacao|presentation|contrato|contract).*\\.(pdf|docx|pptx)$", Pattern.CASE_INSENSITIVE)
        ));
        
        categories.put("PERSONAL_MEDIA", List.of(
            Pattern.compile(".*(foto|photo|img|picture|video|filme|movie).*", Pattern.CASE_INSENSITIVE)
        ));
        
        categories.put("DOWNLOADS", List.of(
            Pattern.compile(".*(download|temp|tmp).*", Pattern.CASE_INSENSITIVE)
        ));
        
        categories.put("BACKUPS", List.of(
            Pattern.compile(".*(backup|bak|old|copy).*", Pattern.CASE_INSENSITIVE)
        ));
        
        categories.put("CONFIGS", List.of(
            Pattern.compile(".*\\.(ini|cfg|conf|config|properties|env)$", Pattern.CASE_INSENSITIVE)
        ));
        
        categories.put("LOGS", Arrays.asList(
            Pattern.compile(".*\\.(log|txt)$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*(log|debug|error).*", Pattern.CASE_INSENSITIVE)
        ));
        
        categories.put("INSTALLERS", Arrays.asList(
            Pattern.compile(".*\\.(exe|msi|dmg|pkg|deb|rpm|apk)$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*(setup|install|installer).*", Pattern.CASE_INSENSITIVE)
        ));
        
        return Collections.unmodifiableMap(categories);
    }
    
    private static Map<String, String> initializeFolderNames() {
        Map<String, String> names = new HashMap<>();
        names.put("PROJECTS", "Projetos");
        names.put("WORK_DOCS", "Documentos de Trabalho");
        names.put("PERSONAL_MEDIA", "Midia Pessoal");
        names.put("DOWNLOADS", "Downloads");
        names.put("BACKUPS", "Backups");
        names.put("CONFIGS", "Configuracoes");
        names.put("LOGS", "Logs");
        names.put("INSTALLERS", "Instaladores");
        names.put("RECENT_FILES", "Arquivos Recentes");
        names.put("OLD_FILES", "Arquivos Antigos");
        names.put("LARGE_FILES", "Arquivos Grandes");
        names.put("SMALL_FILES", "Arquivos Pequenos");
        names.put("UNCATEGORIZED", "Nao Categorizado");
        return Collections.unmodifiableMap(names);
    }
    
    public static String analyzeFile(Path file) {
        String fileName = file.getFileName().toString().toLowerCase();
        
        String patternCategory = findPatternCategory(fileName);
        if (patternCategory != null) {
            return patternCategory;
        }
        
        String dateCategory = analyzeDateCategory(file);
        if (dateCategory != null) {
            return dateCategory;
        }
        
        String sizeCategory = analyzeSizeCategory(file);
        if (sizeCategory != null) {
            return sizeCategory;
        }
        
        return "UNCATEGORIZED";
    }
    
    private static String findPatternCategory(String fileName) {
        for (Map.Entry<String, List<Pattern>> entry : CATEGORIES.entrySet()) {
            for (Pattern pattern : entry.getValue()) {
                if (pattern.matcher(fileName).matches()) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }
    
    private static String analyzeDateCategory(Path file) {
        try {
            long lastModified = Files.getLastModifiedTime(file).toMillis();
            long daysDiff = (System.currentTimeMillis() - lastModified) / MILLIS_PER_DAY;
            
            if (daysDiff <= RECENT_DAYS_THRESHOLD) {
                return "RECENT_FILES";
            } else if (daysDiff > OLD_DAYS_THRESHOLD) {
                return "OLD_FILES";
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }
    
    private static String analyzeSizeCategory(Path file) {
        try {
            long size = Files.size(file);
            if (size > LARGE_FILE_SIZE) {
                return "LARGE_FILES";
            } else if (size < SMALL_FILE_SIZE) {
                return "SMALL_FILES";
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }
    
    public static String getIntelligentFolderName(String category) {
        return FOLDER_NAMES.getOrDefault(category, "Nao Categorizado");
    }
    
    public static Map<String, Integer> analyzeFolder(Path folder) {
        Map<String, Integer> statistics = new HashMap<>();
        
        try (Stream<Path> stream = Files.list(folder)) {
            stream.filter(Files::isRegularFile)
                  .forEach(file -> {
                      String category = analyzeFile(file);
                      statistics.merge(category, 1, Integer::sum);
                  });
        } catch (IOException e) {
            System.err.println("Failed to analyze folder: " + e.getMessage());
        }
        
        return statistics;
    }
}
