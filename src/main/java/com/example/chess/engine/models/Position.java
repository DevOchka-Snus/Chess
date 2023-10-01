package com.example.chess.engine.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class Position {
    private int x;
    private int y;

    public Position(int x, int y) {
        if (x < 1 || x > 8 || y < 1 || y > 8) {
            throw new IllegalArgumentException("Invalid position: " + x + ", " + y);
        }
        this.x = x;
        this.y = y;
    }

    private static Position of(int x, int y) {
        return new Position(x, y);
    }

    public Position upPosition() {
        return y < 8 ? of(x, y + 1) : null;
    }

    public Position downPosition() {
        return y > 1 ? of(x, y - 1) : null;
    }

    public Position rightPosition() {
        return x < 8 ? of(x + 1, y) : null;
    }

    public Position leftPosition() {
        return x > 1 ? of(x - 1, y) : null;
    }

    public Stream<Position> up() {
        return Stream.iterate(this, Objects::nonNull, Position::upPosition).skip(1);
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

    @SafeVarargs
    private Position move(UnaryOperator<Position>... moves) {
        Position finalPosition = this;
        for (int i = 0; i < moves.length && (finalPosition != null); i++) {
            UnaryOperator<Position> move = moves[i];
            finalPosition = move.apply(finalPosition);
        }
        return finalPosition;
    }


}
