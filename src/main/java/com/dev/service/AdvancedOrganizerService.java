package com.dev.service;

import com.dev.model.FileType;
import com.dev.model.OrganizationMode;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class AdvancedOrganizerService {
    
    private static final long MILLIS_PER_DAY = 1000 * 60 * 60 * 24;
    private static final long WEEK_IN_DAYS = 7;
    private static final long MONTH_IN_DAYS = 30;
    private static final long THREE_MONTHS_IN_DAYS = 90;
    private static final long YEAR_IN_DAYS = 365;
    
    private static final long SIZE_10KB = 10 * 1024;
    private static final long SIZE_1MB = 1024 * 1024;
    private static final long SIZE_10MB = 10 * 1024 * 1024;
    private static final long SIZE_100MB = 100 * 1024 * 1024;
    
    private AdvancedOrganizerService() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    public static void organize(List<Path> folders, OrganizationMode mode) {
        folders.forEach(folder -> organizeFolder(folder, mode));
    }
    
    private static void organizeFolder(Path folder, OrganizationMode mode) {
        if (mode == OrganizationMode.BY_PROGRAM) {
            try {
                AIDirectOrganizer.organizeWithAI(folder);
            } catch (IOException e) {
                System.err.println("AI organization failed: " + e.getMessage());
            }
            return;
        }
        
        try (Stream<Path> stream = Files.list(folder)) {
            List<Path> files = stream
                    .filter(Files::isRegularFile)
                    .filter(f -> !f.toString().endsWith(".jar"))
                    .toList();
            
            switch (mode) {
                case BY_TYPE -> organizeByType(files);
                case BY_EXTENSION -> organizeByExtension(files);
                case ALPHABETICAL -> organizeAlphabetically(files);
                case BY_DATE -> organizeByDate(files);
                case BY_SIZE -> organizeBySize(files);
                case INTELLIGENT -> organizeIntelligently(files);
            }
        } catch (IOException e) {
            System.err.println("Failed to list files: " + e.getMessage());
        }
    }
    
    private static void organizeByType(List<Path> files) {
        files.forEach(file -> {
            String name = file.getFileName().toString();
            int index = name.lastIndexOf(".");
            
            if (index <= 0) return;
            
            String ext = name.substring(index + 1);
            FileType type = FileType.fromExtension(ext);
            
            if (type != FileType.OTHERS) {
                moveFile(file, type.name());
            }
        });
    }
    
    private static void organizeByExtension(List<Path> files) {
        files.forEach(file -> {
            String name = file.getFileName().toString();
            int index = name.lastIndexOf(".");
            
            String folder = (index <= 0) ? "SEM_EXTENSAO" : name.substring(index + 1).toUpperCase();
            moveFile(file, folder);
        });
    }
    
    private static void organizeAlphabetically(List<Path> files) {
        files.forEach(file -> {
            String name = file.getFileName().toString();
            char firstChar = Character.toUpperCase(name.charAt(0));
            
            String folder = determineAlphabeticFolder(firstChar);
            moveFile(file, folder);
        });
    }
    
    private static String determineAlphabeticFolder(char firstChar) {
        if (Character.isDigit(firstChar)) return "0-9";
        if (Character.isLetter(firstChar)) return String.valueOf(firstChar);
        return "OUTROS";
    }
    
    private static void organizeByDate(List<Path> files) {
        files.forEach(file -> {
            try {
                BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
                long daysDiff = calculateDaysDifference(attrs.lastModifiedTime().toMillis());
                String folder = determineDateFolder(daysDiff);
                moveFile(file, folder);
            } catch (IOException e) {
                System.err.println("Failed to read date: " + e.getMessage());
            }
        });
    }
    
    private static long calculateDaysDifference(long lastModified) {
        return (System.currentTimeMillis() - lastModified) / MILLIS_PER_DAY;
    }
    
    private static String determineDateFolder(long daysDiff) {
        if (daysDiff <= WEEK_IN_DAYS) return "ULTIMA_SEMANA";
        if (daysDiff <= MONTH_IN_DAYS) return "ULTIMO_MES";
        if (daysDiff <= THREE_MONTHS_IN_DAYS) return "ULTIMOS_3_MESES";
        if (daysDiff <= YEAR_IN_DAYS) return "ULTIMO_ANO";
        return "MAIS_DE_1_ANO";
    }
    
    private static void organizeBySize(List<Path> files) {
        files.forEach(file -> {
            try {
                long size = Files.size(file);
                String folder = determineSizeFolder(size);
                moveFile(file, folder);
            } catch (IOException e) {
                System.err.println("Failed to read size: " + e.getMessage());
            }
        });
    }
    
    private static String determineSizeFolder(long size) {
        if (size < SIZE_10KB) return "PEQUENOS_0-10KB";
        if (size < SIZE_1MB) return "MEDIOS_10KB-1MB";
        if (size < SIZE_10MB) return "GRANDES_1MB-10MB";
        if (size < SIZE_100MB) return "MUITO_GRANDES_10MB-100MB";
        return "ENORMES_100MB+";
    }
    
    private static void organizeIntelligently(List<Path> files) {
        Map<Path, String> categories = LLMOrganizerService.organizeWithLLM(files);
        categories.forEach(AdvancedOrganizerService::moveFile);
    }
    
    private static void moveFile(Path file, String folderName) {
        String name = file.getFileName().toString();
        Path destinationFolder = file.getParent().resolve(folderName);
        
        try {
            Files.createDirectories(destinationFolder);
            Files.move(file, destinationFolder.resolve(name), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            System.err.println("Failed to move file: " + e.getMessage());
        }
    }
}
