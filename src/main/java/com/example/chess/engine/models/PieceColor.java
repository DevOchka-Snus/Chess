package com.example.chess.engine.models;

import com.example.chess.api.models.ColorDto;

public enum PieceColor {
    WHITE,
    BLACK;

    public PieceColor negate() {
        switch (this) {
            case WHITE -> {
                return BLACK;
            }
            case BLACK -> {
                return WHITE;
            }
            default -> throw new IllegalStateException();
        }
    }

    public ColorDto toDto() {
        return this == WHITE ? ColorDto.WHITE : ColorDto.BLACK;
    }
}
