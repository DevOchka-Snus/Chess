package com.example.chess.engine.models;

import com.example.chess.api.models.MoveDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
@Getter
public class PieceMove {
    private final Position from;
    private final Position to;
    private final Type promotion;

    public PieceMove(Position from, Position to) {
        this.from = from;
        this.to = to;
        this.promotion = null;
    }

    public static PieceMove of(MoveDto moveDto) {
        Type promotion = (moveDto.getPromotion() != null && !moveDto.getPromotion().isEmpty())
                ? Type.valueOf(moveDto.getPromotion().toUpperCase())
                : null;
        return new PieceMove(Position.of(moveDto.getFrom()), Position.of(moveDto.getTo()), promotion);
    }

    public MoveDto toDto() {
        return new MoveDto(from.toString(), to.toString(), Optional.ofNullable(promotion).map(Enum::name).orElse(null));
    }
}
