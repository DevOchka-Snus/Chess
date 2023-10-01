package com.example.chess.engine.models.piece;

import com.example.chess.engine.ChessGame;
import com.example.chess.engine.models.PieceColor;
import com.example.chess.engine.models.Position;
import com.example.chess.engine.models.Type;

import java.util.Set;

public class King extends Piece{
    public King(Position position, PieceColor pieceColor) {
        super(position, pieceColor, Type.KING);
    }

    @Override
    public Piece moveTo(Position to) {
        return new King(to, getPieceColor());
    }

    @Override
    public Set<Position> validPieceMoves(ChessGame game) {
        return null;
    }
}
