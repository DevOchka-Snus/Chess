package com.example.chess.exceptions;

public class InvalidMoveException extends GameException {
    public InvalidMoveException() {
        super();
    }

    public InvalidMoveException(String message) {
        super(message);
    }
}
