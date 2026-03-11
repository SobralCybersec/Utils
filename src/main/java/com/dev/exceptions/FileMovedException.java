package com.dev.exceptions;

public class FileMovedException extends RuntimeException {

    public FileMovedException(String message) {
        super("Failed to move file: " + message);
    }
}
