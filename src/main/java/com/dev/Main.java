package com.dev;

import com.dev.cli.FolderSelector;
import com.dev.service.FileOrganizerService;
import com.dev.util.FolderScanner;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public final class Main {
    
    private Main() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void main(String[] args) {
        try {
            Path basePath = Paths.get("").toAbsolutePath();

            List<Path> folders = FolderScanner.listSubFolders(basePath);
            if (folders.isEmpty()) {
                folders = List.of(basePath);
            }

            List<Path> selected = FolderSelector.selectFolders(folders);
            FileOrganizerService.organize(selected);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}