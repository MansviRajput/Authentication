package com.mansvi.auth_backend.exceptions;

public class PasswordMismatchException extends RuntimeException {
    public PasswordMismatchException(String message) { super(message); }
}