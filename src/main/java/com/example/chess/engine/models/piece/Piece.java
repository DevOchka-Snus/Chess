package com.example.chess.engine.models.piece;

import com.example.chess.engine.ChessGame;
import com.example.chess.engine.PieceMove;
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

    public final Set<Position> finallyValidMoves(ChessGame game) {
        Set<Position> moves = validPieceMoves(game);

        moves.removeIf(pos ->
                game.at(pos).map(piece -> piece.pieceColor == pieceColor).orElse(false));

        PieceColor currentPlayer = game.getCurrentPlayer();
        moves.removeIf(m -> game.applyMoveNoValidate(new PieceMove(position, m)).kingUnderAttack(currentPlayer));

        return moves;
    }
}
