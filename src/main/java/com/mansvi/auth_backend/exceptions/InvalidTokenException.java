package com.mansvi.auth_backend.exceptions;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) { super(message); }
}