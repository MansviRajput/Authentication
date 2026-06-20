package com.mansvi.auth_backend.exceptions;

public class AccountNotVerifiedException extends RuntimeException {
    public AccountNotVerifiedException(String message) { super(message); }
}