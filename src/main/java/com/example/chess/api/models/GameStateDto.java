package com.example.chess.api.models;

import lombok.Data;

import java.util.List;

@Data
public class GameStateDto {
    private List<PieceDto> pieces;
    private ColorDto currentPlayer;
    private MoveDto lastOpponentMove;
    private boolean myTurn = false;
    private boolean gameFinished = false;
    private boolean check = false;
    private ColorDto winner = null;
    private String gameFinishedReason = null;
}
