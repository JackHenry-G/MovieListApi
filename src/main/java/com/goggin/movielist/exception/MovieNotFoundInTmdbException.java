package com.goggin.movielist.exception;

public class MovieNotFoundInTmdbException extends Exception {
    public MovieNotFoundInTmdbException(String message) {
        super(message);
    }
}
