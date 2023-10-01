package com.example.chess.engine.models;

import com.example.chess.engine.models.piece.Piece;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
}
