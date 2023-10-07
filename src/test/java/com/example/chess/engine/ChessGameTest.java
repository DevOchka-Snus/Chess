package com.example.chess.engine;

import com.example.chess.engine.models.Board;
import com.example.chess.engine.models.PieceColor;
import com.example.chess.engine.models.PieceMove;
import com.example.chess.engine.models.Position;
import com.example.chess.engine.models.piece.*;
import com.example.chess.exceptions.InvalidMoveException;
import org.mockito.internal.util.collections.Sets;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ChessGameTest {

    @Test
    void testStartGame() {
        ChessGame game = ChessGame.start();

        assertEquals(16, game.getBoard().pieces(PieceColor.WHITE).size());
        assertEquals(16, game.getBoard().pieces(PieceColor.BLACK).size());

        long totalMovesStart = game.getValidMovesForCurrentPlayer().values().stream()
                .mapToLong(Collection::size).sum();
        assertEquals(20, totalMovesStart);
    }

    @Test
    void testMovesInvalid() {
        ChessGame game = ChessGame.start();

        assertThrows(InvalidMoveException.class, () -> {
            movesFromString("E2E7").forEach(game::applyMove);
        });
    }

    @Test
    void testMoves() {
        ChessGame game = ChessGame.start();
        List<PieceMove> moves = movesFromString("E2E4 E7E5 D1H5 B8C6 F1C4 G8F6 H5F7").collect(Collectors.toList());
        for (PieceMove move : moves) {
            game = game.applyMove(move);
            System.out.println(moves.toString());
        }

        assertTrue(game.isFinished());
        assertEquals(GameStatus.CHECKMATE, game.getStatus());
    }

    @Test
    void testValidMovesKnight1() {
        Piece whiteKing = new King(Position.of(2, 2), PieceColor.WHITE);
        Piece blackKnight = new Knight(Position.of(4,3), PieceColor.BLACK);
        Piece whiteKnight = new Knight(Position.of(5,5), PieceColor.WHITE);

        Board board = new Board(Arrays.asList(whiteKing, whiteKnight, blackKnight));

        ChessGame game = new ChessGame(PieceColor.WHITE, Collections.emptyList(), Collections.emptyList(), board);
        Set<Position> validMoves = whiteKnight.finallyValidMoves(game);

        assertEquals(Sets.newSet(blackKnight.getPosition()), validMoves);
    }

    @Test
    void testValidMovesRock1() {
        Piece rock = new Rock(Position.of(5,5), PieceColor.WHITE);
        Piece k1 = new Knight(Position.of(5, 6), PieceColor.WHITE);
        Piece k2 = new Knight(Position.of(6, 5), PieceColor.WHITE);
        Piece ki = new King(Position.of(2, 5), PieceColor.WHITE);
        Piece k4 = new Knight(Position.of(5, 4), PieceColor.BLACK);

        Board board = new Board(Arrays.asList(rock, k1, k2, ki, k4));
        ChessGame game = new ChessGame(PieceColor.WHITE, Collections.emptyList(), Collections.emptyList(), board);
        Set<Position> validMoves = rock.finallyValidMoves(game);

        assertEquals(Sets.newSet(k4.getPosition(), ki.getPosition().rightPosition(), ki.getPosition().rightPosition().rightPosition()), validMoves);
    }

    @Test
    void testValidMovesPawn1() {
        Piece ki = new King(Position.of(5, 1), PieceColor.WHITE);
        Piece kib = new King(Position.of(5, 8), PieceColor.BLACK);
        Piece p1 = new Pawn(Position.of(5,2), PieceColor.WHITE);
        Piece p2 = new Pawn(Position.of(5,4), PieceColor.BLACK);
        Piece p3 = new Pawn(Position.of(6,3), PieceColor.BLACK);
        Piece pe = new Pawn(Position.of(6,4), PieceColor.BLACK);

        Board board = new Board(Arrays.asList(ki, p1));
        ChessGame game = new ChessGame(PieceColor.WHITE, Collections.emptyList(), Collections.emptyList(), board);
        Set<Position> validMoves = p1.finallyValidMoves(game);

        assertEquals(Sets.newSet(p1.getPosition().upPosition(), p1.getPosition().upPosition().upPosition()), validMoves);

        board = new Board(Arrays.asList(ki, p1, p2, p3));
        game = new ChessGame(PieceColor.WHITE, Collections.emptyList(), Collections.emptyList(), board);
        validMoves = p1.finallyValidMoves(game);

        assertEquals(Sets.newSet(p1.getPosition().upPosition(), p3.getPosition()), validMoves);

        board = new Board(Arrays.asList(ki, kib, p1, pe));
        PieceMove e2e4 = new PieceMove(Position.of(5, 2), Position.of(5,4));
        board = board.applyMoveNoValidate(e2e4);
        game = new ChessGame(PieceColor.BLACK, Collections.emptyList(), Collections.singletonList(e2e4), board);
        validMoves = pe.validPieceMoves(game);

        assertEquals(Sets.newSet(pe.getPosition().diagDownLeftPosition(), pe.getPosition().downPosition()), validMoves);

        board = board.applyMoveNoValidate(new PieceMove(pe.getPosition(), Position.of(5,3)));
        assertNull(board.pieceMap(PieceColor.WHITE).get(Position.of(5,3)));
    }

    @Test
    void testValidMovesKing1() {
        Piece r1 = new Rock(Position.of(1, 1), PieceColor.WHITE);
        Piece r2 = new Rock(Position.of(8, 1), PieceColor.WHITE);
        Piece king = new King(Position.of(5, 1), PieceColor.WHITE);
        Piece knight = new Knight(Position.of(5, 8), PieceColor.BLACK);
        Piece r = new Rock(Position.of(1, 2), PieceColor.BLACK);
        Piece q = new Queen(Position.of(4, 8), PieceColor.BLACK);

        Board board = new Board(Arrays.asList(r1, r2, king, knight, r));
        ChessGame game = new ChessGame(PieceColor.WHITE, Collections.emptyList(), Collections.emptyList(), board);
        Set<Position> validMoves = king.finallyValidMoves(game);

        Position kp = king.getPosition();

        assertEquals(Sets.newSet(kp.leftPosition(), kp.leftPosition().leftPosition(), kp.rightPosition(), kp.rightPosition().rightPosition()), validMoves);

        board = new Board(Arrays.asList(r1, r2, king, knight, r, q));

        game = new ChessGame(PieceColor.WHITE, Collections.emptyList(), Collections.emptyList(), board);
        validMoves = king.finallyValidMoves(game);

        assertEquals(Sets.newSet(kp.rightPosition(), kp.rightPosition().rightPosition()), validMoves);
    }

    public static Stream<PieceMove> movesFromString(String moves) {
        return Arrays.stream(moves.split("\\s+"))
                .map(s -> {
                    Position from = Position.of(s.substring(0,2));
                    Position to = Position.of(s.substring(2));
                    return  new PieceMove(from, to);
                });
    }
}
