package com.dev.cli;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public final class FolderSelector {
    
    private FolderSelector() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static List<Path> selectFolders(List<Path> folders) {
        try (Scanner scanner = new Scanner(System.in)) {
            List<Path> selected = new ArrayList<>();

            for (int i = 0; i < folders.size(); i++) {
                System.out.println(i + " - " + folders.get(i).getFileName());
            }

            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("all")) {
                return folders;
            }

            String[] indices = input.split(",");
            for (String index : indices) {
                try {
                    int i = Integer.parseInt(index.trim());
                    selected.add(folders.get(i));
                } catch (Exception e) {
                    System.err.println("Invalid index: " + index);
                }
            }

            return selected;
        }
    }
}