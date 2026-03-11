package com.dev.service;

import com.dev.model.FileType;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Stream;

public final class FileOrganizerService {
    
    private FileOrganizerService() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void organize(List<Path> folders) {
        folders.forEach(FileOrganizerService::organizeFolder);
    }
    
    private static void organizeFolder(Path folder) {
        try (Stream<Path> stream = Files.list(folder)) {
            stream.filter(Files::isRegularFile)
                  .filter(f -> !f.toString().endsWith(".jar"))
                  .forEach(FileOrganizerService::moveByType);
        } catch (IOException e) {
            System.err.println("Failed to list files: " + e.getMessage());
        }
    }

    private static void moveByType(Path file) {
        String name = file.getFileName().toString();
        int index = name.lastIndexOf(".");
        
        if (index <= 0) return;

        String ext = name.substring(index + 1);
        FileType type = FileType.fromExtension(ext);
        
        if (type == FileType.OTHERS) return;

        Path destinationFolder = file.getParent().resolve(type.name());

        try {
            Files.createDirectories(destinationFolder);
            Files.move(file, destinationFolder.resolve(name), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            System.err.println("Failed to move file: " + e.getMessage());
        }
    }
}