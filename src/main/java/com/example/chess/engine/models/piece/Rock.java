package com.example.chess.engine.models.piece;

import com.example.chess.engine.ChessGame;
import com.example.chess.engine.models.PieceColor;
import com.example.chess.engine.models.Position;
import com.example.chess.engine.models.Type;

import java.util.Set;

public class Rock extends Piece{

    public Rock(Position position, PieceColor pieceColor) {
        super(position, pieceColor, Type.ROCK);
    }

    @Override
    public Piece moveTo(Position to) {
        return new Rock(to, getPieceColor());
    }

    @Override
    public Set<Position> validPieceMoves(ChessGame game) {
        return position.moveUntilHit(position.rock(), game, pieceColor);
    }
}
