package com.example.chess.engine.models.piece;

import com.example.chess.engine.ChessGame;
import com.example.chess.engine.models.PieceColor;
import com.example.chess.engine.models.Position;
import com.example.chess.engine.models.Type;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Getter
@RequiredArgsConstructor
public abstract class Piece {
    protected final Position position;
    protected final PieceColor pieceColor;
    protected final Type pieceType;

    public abstract Piece moveTo(Position to);
    public abstract Set<Position> validPieceMoves(ChessGame game);
}
