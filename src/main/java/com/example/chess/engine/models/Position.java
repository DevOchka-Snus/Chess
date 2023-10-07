package com.example.chess.engine.models;

import com.example.chess.engine.ChessGame;
import com.example.chess.engine.models.piece.Piece;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

@Getter
@EqualsAndHashCode
public class Position {
    private final int x;
    private final int y;

    public Position(int x, int y) {
        if (x < 1 || x > 8 || y < 1 || y > 8) {
            throw new IllegalArgumentException("Invalid position: " + x + ", " + y);
        }
        this.x = x;
        this.y = y;
    }

    public static Position of(int x, int y) {
        return new Position(x, y);
    }

    public static Position of(String s) {
        if (!s.matches("(?i)[A-H][1-8]")) {
            throw new IllegalArgumentException("Position invalid: " + s);
        }

        int x = switch (s.toLowerCase().charAt(0)) {
            case 'a' -> 1;
            case 'b' -> 2;
            case 'c' -> 3;
            case 'd' -> 4;
            case 'e' -> 5;
            case 'f' -> 6;
            case 'g' -> 7;
            case 'h' -> 8;
            default -> throw new IllegalStateException("Unexpected value: " + s.toLowerCase().charAt(0));
        };

        int y = Integer.parseInt(s.substring(1));
        return new Position(x, y);
    }

    public Position upPosition() {
        return (y < 8) ? of(x, y + 1) : null;
    }

    public Position downPosition() {
        return (y > 1) ? of(x, y - 1) : null;
    }

    public Position rightPosition() {
        return (x < 8) ? of(x + 1, y) : null;
    }

    public Position leftPosition() {
        return (x > 1) ? of(x - 1, y) : null;
    }

    public Position diagDownLeftPosition() {
        return move(Position::downPosition, Position::leftPosition);
    }

    public Position diagDownRightPosition() {
        return move(Position::downPosition, Position::rightPosition);
    }

    public Position diagUpLeftPosition() {
        return move(Position::upPosition, Position::leftPosition);
    }

    public Position diagUpRightPosition() {
        return move(Position::upPosition, Position::rightPosition);
    }

    @SafeVarargs
    private Position move(UnaryOperator<Position>... moves) {
        Position finalPosition = this;
        for (int i = 0; i < moves.length && (finalPosition != null); i++) {
            UnaryOperator<Position> move = moves[i];
            finalPosition = move.apply(finalPosition);
        }
        return finalPosition;
    }

    public List<Position> king() {
        List<Position> moves = new ArrayList<>();
        moves.add(upPosition());
        moves.add(downPosition());
        moves.add(leftPosition());
        moves.add(rightPosition());
        moves.add(diagDownLeftPosition());
        moves.add(diagDownRightPosition());
        moves.add(diagUpLeftPosition());
        moves.add(diagUpRightPosition());

        moves.removeIf(Objects::isNull);

        return moves;
    }

    public List<Position> knight() {
        List<Position> moves = new ArrayList<>();
        moves.add(move(Position::upPosition, Position::upPosition, Position::rightPosition));
        moves.add(move(Position::upPosition, Position::upPosition, Position::leftPosition));
        moves.add(move(Position::downPosition, Position::downPosition, Position::rightPosition));
        moves.add(move(Position::downPosition, Position::downPosition, Position::leftPosition));
        moves.add(move(Position::leftPosition, Position::leftPosition, Position::upPosition));
        moves.add(move(Position::leftPosition, Position::leftPosition, Position::downPosition));
        moves.add(move(Position::rightPosition, Position::rightPosition, Position::upPosition));
        moves.add(move(Position::rightPosition, Position::rightPosition, Position::downPosition));

        moves.removeIf(Objects::isNull);

        return moves;
    }

    public List<Stream<Position>> bishop() {
        return Arrays.asList(diagDownLeft(), diagDownRight(), diagUpLeft(), diagUpRight());
    }

    public List<Stream<Position>> queen() {
        List<Stream<Position>> moveLines = new ArrayList<>();
        moveLines.addAll(rock());
        moveLines.addAll(bishop());

        return moveLines;
    }

    public List<Stream<Position>> rock() {
        return Arrays.asList(up(), down(), left(), right());
    }

    public Stream<Position> up() {
        return Stream.iterate(this, Objects::nonNull, Position::upPosition).skip(1);
    }

    private Stream<Position> right() {
        return Stream.iterate(this, Objects::nonNull, Position::rightPosition).skip(1);
    }

    private Stream<Position> left() {
        return Stream.iterate(this, Objects::nonNull, Position::leftPosition).skip(1);
    }

    private Stream<Position> down() {
        return Stream.iterate(this, Objects::nonNull, Position::downPosition).skip(1);
    }

    private Stream<Position> diagUpRight() {
        return Stream.iterate(this, Objects::nonNull, Position::diagUpRightPosition).skip(1);
    }

    private Stream<Position> diagUpLeft() {
        return Stream.iterate(this, Objects::nonNull, Position::diagUpLeftPosition).skip(1);
    }

    private Stream<Position> diagDownRight() {
        return Stream.iterate(this, Objects::nonNull, Position::diagDownRightPosition).skip(1);
    }

    private Stream<Position> diagDownLeft() {
        return Stream.iterate(this, Objects::nonNull, Position::diagDownLeftPosition).skip(1);
    }

    public Set<Position> moveUntilHit(List<Stream<Position>> moveLines, ChessGame game, PieceColor pieceColor) {
        Set<Position> validMoves = new HashSet<>();
        moveLines.forEach(moveLine -> {
            for (Position to : (Iterable<Position>) moveLine::iterator) {
                Optional<Piece> pieceAtPosition = game.at(to);

                if (pieceAtPosition.isEmpty() || pieceAtPosition.get().getPieceColor() != pieceColor) {
                    validMoves.add(to);
                }
                if (pieceAtPosition.isPresent()) {
                    break;
                }
            }
        });

        return validMoves;
    }

    @Override
    public String toString() {
        return switch (x) {
            case 1 -> "A" + y;
            case 2 -> "B" + y;
            case 3 -> "C" + y;
            case 4 -> "D" + y;
            case 5 -> "E" + y;
            case 6 -> "F" + y;
            case 7 -> "G" + y;
            case 8 -> "H" + y;
            default -> throw new IllegalStateException("Unexpected value: " + x);
        };
    }
}
