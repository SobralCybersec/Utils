package com.dev.exceptions;

public class UnsupportedFileTypeException extends RuntimeException {

    public UnsupportedFileTypeException(String file) {
        super("Unsupported file type for file: " + file);
    }
}