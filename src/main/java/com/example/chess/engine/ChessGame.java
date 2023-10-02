package com.example.chess.engine;

import com.example.chess.engine.models.Board;
import com.example.chess.engine.models.PieceColor;
import com.example.chess.engine.models.Position;
import com.example.chess.engine.models.piece.Piece;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor
public class ChessGame {
    private final PieceColor currentPlayer;
    private final List<Board> previousStates;
    private final List<PieceMove> previousMoves;
    private final Board board;

    private boolean finished = false;
    private PieceColor winner = null;
    private GameStatus status = GameStatus.NORMAL;
    private Map<Position, Set<Position>> validMovesForCurrentPlayer;

    public Optional<Piece> at(Position position) {
        return board.at(position);
    }

    public boolean isFinished() {
        return finished;
    }

    public ChessGame applyMoveNoValidate(PieceMove pieceMove) {
        Board boardAfterMove = board.applyMoveNoValidate(pieceMove);
        List<Board> states = new ArrayList<>(previousStates);
        states.add(board);

        List<PieceMove> moves = new ArrayList<>(previousMoves);
        moves.add(pieceMove);

        return new ChessGame(currentPlayer.negate(), states, moves, boardAfterMove);
    }

    public boolean kingUnderAttack(PieceColor player) {
        Set<Position> attackPositions =
                board.pieces(player.negate()).stream()
                        .flatMap(p -> p.validPieceMoves(this).stream())
                        .collect(Collectors.toSet());
        return attackPositions.contains(board.king(player));
    }

    public void updateGameStatus() {
        this.validMovesForCurrentPlayer = validMovesForCurrentPlayer();

        boolean kingAttacked = kingUnderAttack(currentPlayer);
        boolean canMove = validMovesForCurrentPlayer.values().stream().anyMatch(s -> !s.isEmpty());

        if (kingAttacked && canMove) {
            status = GameStatus.CHECK;
        }
        if (!canMove && kingAttacked) {
            status = GameStatus.CHECKMATE;
            finished = true;
        }
        if (!canMove && !kingAttacked) {
            status = GameStatus.DRAW_STALEMATE;
            finished = true;
        }
    }

    private Map<Position, Set<Position>> validMovesForCurrentPlayer() {
        return board.pieceMap(currentPlayer).values().stream()
                .collect(Collectors.toMap(Piece::getPosition, p -> p.finallyValidMoves(this)));
    }

    public boolean empty(Position position) {
        return board.at(position).isEmpty();
    }

    public boolean isUnderAttack(Position position) {
        Set<Position> attackPositions =
                board.pieces(currentPlayer.negate()).stream()
                        .flatMap(p -> p.validPieceMoves(this).stream())
                        .collect(Collectors.toSet());
        return attackPositions.contains(position);
    }
}
