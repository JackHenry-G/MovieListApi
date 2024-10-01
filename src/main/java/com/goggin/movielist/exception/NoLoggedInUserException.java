package com.goggin.movielist.exception;

public class NoLoggedInUserException extends Exception {
    public NoLoggedInUserException(String message) {
        super(message);
    }
}
