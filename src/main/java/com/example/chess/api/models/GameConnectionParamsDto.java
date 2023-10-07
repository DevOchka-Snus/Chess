package com.example.chess.api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameConnectionParamsDto {
    private String id;
    private String token;
    private ColorDto playerSide;
    private GameStateDto gameState;

}
