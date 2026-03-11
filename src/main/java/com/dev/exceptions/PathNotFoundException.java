package com.dev.exceptions;

public class PathNotFoundException extends RuntimeException {
    public PathNotFoundException(String message) {

        super("Path not found: " + message);
    }
}
