package com.example.chess.exceptions;

public class GameNotFoundException extends GameException {
    public GameNotFoundException() {
        super();
    }

    public GameNotFoundException(String message) {
        super(message);
    }
}
