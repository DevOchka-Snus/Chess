package com.example.chess.engine.models;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PositionTest {

    @Test
    void testKnight1() {
        Position position = Position.of(1,1);
        List<Position> knights = position.knight();
        assertEquals(2, knights.size());
        assertTrue(knights.contains(Position.of(3, 2)));
        assertTrue(knights.contains(Position.of(2, 3)));
    }

    @Test
    void testKnight2() {
        Position p = Position.of(5, 5);

        Set<Position> rock = p.rock().stream().flatMap(s -> s).collect(Collectors.toSet());
        assertEquals(14, rock.size());
    }

    @Test
    void testConvert() {
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                assertEquals(Position.of(i, j), Position.of(Position.of(i, j).toString()));
            }
        }
    }

    @Test
    void testTricky() {
        assertEquals(8, tricky(5, 5).size());
    }

    private List<Position> tricky(int x, int y) {
        List<Position> res = new ArrayList<>();

        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                int mx = Math.abs(x - i);
                int my = Math.abs(y - j);
                if ((mx + my == 3) && (mx * my == 2)
                        && (Math.abs(i - 4.5) < 4.5)
                        && (Math.abs(j - 4.5) < 4.5)
                ) {
                    res.add(Position.of(i, j));
                }
            }
        }

        return res;
    }
}
