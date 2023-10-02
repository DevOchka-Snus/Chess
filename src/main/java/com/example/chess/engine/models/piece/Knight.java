package com.example.chess.engine.models.piece;

import com.example.chess.engine.ChessGame;
import com.example.chess.engine.models.PieceColor;
import com.example.chess.engine.models.Position;
import com.example.chess.engine.models.Type;

import java.util.HashSet;
import java.util.Set;

public class Knight extends Piece{

    public Knight(Position position, PieceColor pieceColor) {
        super(position, pieceColor, Type.KNIGHT);
    }

    @Override
    public Piece moveTo(Position to) {
        return new Knight(to, getPieceColor());
    }

    @Override
    public Set<Position> validPieceMoves(ChessGame game) {
        return new HashSet<>(position.knight());
    }
}
