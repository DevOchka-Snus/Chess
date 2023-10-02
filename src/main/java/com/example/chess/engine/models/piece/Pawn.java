package com.example.chess.engine.models.piece;

import com.example.chess.engine.ChessGame;
import com.example.chess.engine.PieceMove;
import com.example.chess.engine.models.PieceColor;
import com.example.chess.engine.models.Position;
import com.example.chess.engine.models.Type;

import java.util.*;
import java.util.function.UnaryOperator;

public class Pawn extends Piece{
    final static Map<PieceColor, Integer> START_LINE = Map.of(
            PieceColor.WHITE, 2,
            PieceColor.BLACK, 7
    );

    final static Map<PieceColor, Integer> ENPASSANT_LINE = Map.of(
            PieceColor.WHITE, 5,
            PieceColor.BLACK, 4
    );

    public final static Map<PieceColor, UnaryOperator<Position>> MOVE_FORWARD = Map.of(
            PieceColor.WHITE, Position::upPosition,
            PieceColor.BLACK, Position::downPosition
    );

    public final static  Map<PieceColor, UnaryOperator<Position>> MOVE_BACKWARD = Map.of(
            PieceColor.WHITE, Position::downPosition,
            PieceColor.BLACK, Position::upPosition
    );

    public Pawn(Position position, PieceColor pieceColor) {
        super(position, pieceColor, Type.PAWN);
    }

    @Override
    public Piece moveTo(Position to) {
        return new Pawn(to, getPieceColor());
    }

    @Override
    public Set<Position> validPieceMoves(ChessGame game) {
        Set<Position> moves = new HashSet<>();

        UnaryOperator<Position> moveForward = MOVE_FORWARD.get(pieceColor);
        Position f1 = moveForward.apply(position);

        if (f1 == null) {
            return Collections.emptySet();
        }

        Position f2 = moveForward.apply(f1);

        Position fl = f1.leftPosition();
        Position fr = f1.rightPosition();

        if (game.empty(f1)) {
            moves.add(f1);
        }
        if (fl != null && game.at(fl).map(p -> p.pieceColor != pieceColor).orElse(false)) {
            moves.add(fl);
        }

        if (fr != null && game.at(fr).map(p -> p.pieceColor != pieceColor).orElse(false)) {
            moves.add(fr);
        }

        if (isAtStartLine() && game.empty(f1) && game.empty(f2)) {
            moves.add(f2);
        }

        enPassant(game).ifPresent(moves::add);
        return moves;
    }

    private Optional<Position> enPassant(ChessGame game) {
        if (!ENPASSANT_LINE.get(pieceColor).equals(position.getY())
            || game.getPreviousMoves().isEmpty()) {
            return Optional.empty();
        }

        UnaryOperator<Position> moveForward = MOVE_FORWARD.get(pieceColor);
        UnaryOperator<Position> moveBackward = MOVE_BACKWARD.get(pieceColor);
        Position fl = moveForward.apply(position).leftPosition();
        Position fr = moveForward.apply(position).rightPosition();

        for (Position forwardAttack : new Position[] {fl, fr}) {
            if (forwardAttack != null) {
                Position near = moveBackward.apply(forwardAttack);
                Position nearFrom = moveBackward.apply(forwardAttack);

                PieceMove lastOppMove = game.getPreviousMoves().get(game.getPreviousMoves().size() - 1);
                if (game.at(near).map(p -> p.getPieceType() == Type.PAWN && p.pieceColor != pieceColor).orElse(false)
                    && lastOppMove.getFrom().equals(nearFrom)
                    && lastOppMove.getTo().equals(near)) {
                    return Optional.of(forwardAttack);
                }
            }
        }
        return Optional.empty();
    }

    private boolean isAtStartLine() {
        return START_LINE.get(pieceColor).equals(position.getY());
    }
}
