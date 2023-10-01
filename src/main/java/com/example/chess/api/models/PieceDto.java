package com.example.chess.api.models;

import lombok.Data;

import java.util.List;

@Data
public class PieceDto {
    private String position;
    private ColorDto color;
    private PieceTypeDto pieceType;

    private List<String> validMoves;
}
