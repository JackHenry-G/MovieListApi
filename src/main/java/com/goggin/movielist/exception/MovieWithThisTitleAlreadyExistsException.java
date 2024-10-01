package com.goggin.movielist.exception;

public class MovieWithThisTitleAlreadyExistsException extends Exception {
    public MovieWithThisTitleAlreadyExistsException(String message) {
        super(message);
    }
}
