package com.example.chess.engine.models.piece;

import com.example.chess.engine.ChessGame;
import com.example.chess.engine.models.PieceColor;
import com.example.chess.engine.models.Position;
import com.example.chess.engine.models.Type;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class King extends Piece{
    private static final Map<PieceColor, Position> START_POSITION = Map.of(
            PieceColor.WHITE, Position.of(5, 1),
            PieceColor.BLACK, Position.of(5, 8)
    );

    public King(Position position, PieceColor pieceColor) {
        super(position, pieceColor, Type.KING);
    }

    @Override
    public Piece moveTo(Position to) {
        return new King(to, getPieceColor());
    }

    @Override
    public Set<Position> validPieceMoves(ChessGame game) {
        Set<Position> moves = new HashSet<>();
        moves.addAll(position.king());
        if (game.getCurrentPlayer() == pieceColor) {
            moves.addAll(castling(game).collect(Collectors.toList()));
        }

        return moves;
    }

    private Stream<Position> castling(ChessGame game) {
        if (position.equals(START_POSITION.get(pieceColor)) && neverMoved(position, game)) {
            return Stream.of(castlingLeft(game), castlingRight(game))
                    .filter(Optional::isPresent)
                    .map(Optional::get);
        } else {
            return Stream.empty();
        }
    }

    private Optional<Position> castlingRight(ChessGame game) {
        Position r1 = position.rightPosition();
        Position r2 = r1.rightPosition();
        Position rock = r2.rightPosition();

        if (game.empty(r1)
            && game.empty(r2)
            && neverMoved(rock, game)
            && !game.isUnderAttack(r1)
            && !game.isUnderAttack(r2)) {
            return Optional.of(r2);
        }

        return Optional.empty();
    }

    private Optional<Position> castlingLeft(ChessGame game) {
        Position l1 = position.leftPosition();
        Position l2 = l1.leftPosition();
        Position l3 = l2.leftPosition();
        Position rock = l3.leftPosition();

        if (game.empty(l1)
            && game.empty(l2)
            && game.empty(l3)
            && neverMoved(rock, game)
            && !game.isUnderAttack(l1)
            && !game.isUnderAttack(l2)) {
            return Optional.of(l2);
        }

        return Optional.empty();
    }

    private boolean neverMoved(Position position, ChessGame game) {
        return game.getPreviousMoves().stream()
                .noneMatch(m -> m.getFrom().equals(position));
    }
}
