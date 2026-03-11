package com.dev.util;

import com.dev.exceptions.PathNotFoundException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public final class FolderScanner {
    
    private FolderScanner() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static List<Path> listSubFolders(Path basePath) {
        if (!Files.exists(basePath)) {
            throw new PathNotFoundException(basePath.toString());
        }

        try (Stream<Path> stream = Files.list(basePath)) {
            return stream.filter(Files::isDirectory).toList();
        } catch (IOException e) {
            throw new RuntimeException("Failed to list subfolders: " + basePath, e);
        }
    }

    public static List<Path> listAllFilesRecursively(Path basePath) {
        if (!Files.exists(basePath)) {
            throw new PathNotFoundException(basePath.toString());
        }

        try (Stream<Path> stream = Files.walk(basePath)) {
            return stream.filter(Files::isRegularFile).toList();
        } catch (IOException e) {
            throw new RuntimeException("Failed to scan files: " + basePath, e);
        }
    }
}