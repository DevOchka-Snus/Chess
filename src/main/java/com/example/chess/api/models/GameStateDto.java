package com.example.chess.api.models;

import java.util.List;

public class GameStateDto {
    private List<PieceDto> pieces;
    private ColorDto currentPlayer;

    private MoveDto lastOpponentMove;


}
