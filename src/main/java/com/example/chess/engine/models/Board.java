package com.example.chess.engine.models;

import com.example.chess.engine.models.piece.*;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NoArgsConstructor
public class Board implements Cloneable{
    final Map<Position, Piece> whitePieces = new HashMap<>();
    final Map<Position, Piece> blackPieces = new HashMap<>();

    public Board(Collection<Piece> pieces) {
        whitePieces.putAll(pieces.stream().filter(piece -> piece.getPieceColor() == PieceColor.WHITE)
                .collect(Collectors.toMap(Piece::getPosition, Function.identity())));
        blackPieces.putAll(pieces.stream().filter(piece -> piece.getPieceColor() == PieceColor.BLACK)
                .collect(Collectors.toMap(Piece::getPosition, Function.identity())));
    }

    public static Board startBoard() {
        Board board = new Board();

        Stream.iterate(Position.of(1, 2), Position::rightPosition).limit(8)
                .forEach(p -> board.put(new Pawn(p, PieceColor.WHITE)));

        Stream.iterate(Position.of(1, 7), Position::rightPosition).limit(8)
                .forEach(p -> board.put(new Pawn(p, PieceColor.BLACK)));

        board.put(new Rock(Position.of(1, 1), PieceColor.WHITE));
        board.put(new Rock(Position.of(8, 1), PieceColor.WHITE));
        board.put(new Rock(Position.of(1, 8), PieceColor.BLACK));
        board.put(new Rock(Position.of(8, 8), PieceColor.BLACK));

        board.put(new Knight(Position.of(2, 1), PieceColor.WHITE));
        board.put(new Knight(Position.of(7, 1), PieceColor.WHITE));
        board.put(new Knight(Position.of(2, 8), PieceColor.BLACK));
        board.put(new Knight(Position.of(7, 8), PieceColor.BLACK));

        board.put(new Bishop(Position.of(3, 1), PieceColor.WHITE));
        board.put(new Bishop(Position.of(6, 1), PieceColor.WHITE));
        board.put(new Bishop(Position.of(3, 8), PieceColor.BLACK));
        board.put(new Bishop(Position.of(6, 8), PieceColor.BLACK));

        board.put(new Queen(Position.of(4, 1), PieceColor.WHITE));
        board.put(new Queen(Position.of(4, 8), PieceColor.BLACK));

        board.put(new King(Position.of(5, 1), PieceColor.WHITE));
        board.put(new King(Position.of(5, 8), PieceColor.BLACK));

        return board;
    }

    void put(Piece piece) {
        switch (piece.getPieceColor()) {
            case WHITE -> whitePieces.put(piece.getPosition(), piece);
            case BLACK -> blackPieces.put(piece.getPosition(), piece);
        }
    }

    public Optional<Piece> at(Position position) {
        return whitePieces.containsKey(position)
                ? Optional.of(whitePieces.get(position))
                : Optional.ofNullable(blackPieces.get(position));
    }

    public Board applyMoveNoValidate(PieceMove pieceMove) {
        Board cloned = this.clone();
        if (isCastling(pieceMove)) {
            cloned.applyCastling(pieceMove);
        } else if (isEnPassant(pieceMove)) {
            cloned.applyEnPassant(pieceMove);
        } else {
            cloned.applyStandardMove(pieceMove);
        }

        if (pieceMove.getPromotion() != null) {
            cloned.applyPromotion(pieceMove);
        }

        return cloned;
    }

    private void applyPromotion(PieceMove pieceMove) {
        PieceColor pieceColor = at(pieceMove.getTo()).orElseThrow().getPieceColor();
        pieceMap(pieceColor).put(pieceMove.getTo(),
                pieceMove.getPromotion().newPiece(pieceMove.getTo(), pieceColor));
    }

    private void applyStandardMove(PieceMove pieceMove) {
        Position from = pieceMove.getFrom();
        Position to = pieceMove.getTo();
        PieceColor color = at(from).orElseThrow().getPieceColor();

        Map<Position, Piece> playerPieces = pieceMap(color);
        Map<Position, Piece> opponentPieces = pieceMap(color.negate());

        opponentPieces.remove(to);
        moveTo(playerPieces, from, to);
    }

    private void applyEnPassant(PieceMove pieceMove) {
        PieceColor pieceColor = at(pieceMove.getFrom()).orElseThrow().getPieceColor();

        UnaryOperator<Position> moveBackward = Pawn.MOVE_BACKWARD.get(pieceColor);
        Position near = moveBackward.apply(pieceMove.getTo());

        moveTo(pieceMap(pieceColor), pieceMove.getFrom(), pieceMove.getTo());
        pieceMap(pieceColor.negate()).remove(near);
    }

    private boolean isEnPassant(PieceMove pieceMove) {
        return at(pieceMove.getFrom()).orElseThrow().getPieceType() == Type.PAWN
                && pieceMove.getFrom().getX() != pieceMove.getTo().getX()
                && at(pieceMove.getTo()).isEmpty();
    }

    private void applyCastling(PieceMove pieceMove) {
        var from = pieceMove.getFrom();
        var to = pieceMove.getTo();
        Position rockFrom;
        Position rockTo;

        if (to.getX() < from.getX()) {
            rockFrom = to.leftPosition().leftPosition();
            rockTo = to.rightPosition();
        } else {
            rockFrom = to.rightPosition();
            rockTo = to.leftPosition();
        }

        PieceColor color = at(from).orElseThrow().getPieceColor();
        Map<Position, Piece> playersPieces = pieceMap(color);

        moveTo(playersPieces, from, to);
        moveTo(playersPieces, rockFrom, rockTo);
    }

    private void moveTo(Map<Position, Piece> playersPieces, Position from, Position to) {
        playersPieces.put(to, playersPieces.get(from).moveTo(to));
        playersPieces.remove(from);
    }

    public Map<Position, Piece> pieceMap(PieceColor side) {
        return side == PieceColor.WHITE ? whitePieces : blackPieces;
    }

    private boolean isCastling(PieceMove pieceMove) {
        return at(pieceMove.getFrom()).orElseThrow().getPieceType() == Type.KING
                && Math.abs(pieceMove.getFrom().getX() - pieceMove.getTo().getX()) > 1;
    }

    @Override
    protected Board clone() {
        Board cloned = new Board();
        cloned.whitePieces.putAll(whitePieces);
        cloned.blackPieces.putAll(blackPieces);
        return cloned;
    }

    public Collection<Piece> pieces() {
        return Stream.of(whitePieces.values(), blackPieces.values())
                .flatMap(Collection::stream).collect(Collectors.toList());
    }

    public Collection<Piece> pieces(PieceColor side) {
        return side == PieceColor.WHITE ? whitePieces.values() : blackPieces.values();
    }

    public Position king(PieceColor side) {
        return pieces(side).stream()
                .filter(p -> p.getPieceType() == Type.KING)
                .findAny()
                .map(Piece::getPosition)
                .orElseThrow();
    }
}
