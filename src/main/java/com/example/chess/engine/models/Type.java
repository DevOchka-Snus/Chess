package com.example.chess.engine.models;

import com.example.chess.engine.models.piece.*;

public enum Type {
    KING {
        @Override
        public Piece newPiece(Position position, PieceColor pieceColor) {
            return new King(position, pieceColor);
        }
    },
    QUEEN {
        @Override
        public Piece newPiece(Position position, PieceColor pieceColor) {
            return new Queen(position, pieceColor);
        }
    },
    ROCK {
        @Override
        public Piece newPiece(Position position, PieceColor pieceColor) {
            return new Rock(position, pieceColor);
        }
    },
    BISHOP {
        @Override
        public Piece newPiece(Position position, PieceColor pieceColor) {
            return new Bishop(position, pieceColor);
        }
    },
    KNIGHT {
        @Override
        public Piece newPiece(Position position, PieceColor pieceColor) {
            return new Knight(position, pieceColor);
        }
    },
    PAWN {
        @Override
        public Piece newPiece(Position position, PieceColor pieceColor) {
            return new Pawn(position, pieceColor);
        }
    };

    public Piece newPiece(Position to, PieceColor pieceColor) {
        throw new UnsupportedOperationException();
    }
}
