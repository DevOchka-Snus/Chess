package com.example.chess.exceptions;

public class InvalidTokenException extends GameException {
    public InvalidTokenException() {
        super();
    }

    public InvalidTokenException(String message) {
        super(message);
    }
}
