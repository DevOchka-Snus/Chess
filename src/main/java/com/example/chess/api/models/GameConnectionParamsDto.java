package com.example.chess.api.models;

import lombok.Data;

@Data
public class GameConnectionParamsDto {
    private String id;
    private String token;
    private ColorDto playerSide;
    private GameStateDto gameState;
}
