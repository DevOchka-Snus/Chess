package com.example.chess.engine.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public class GamePlayerDesc {
    private final String gameId;
    private final String playerToken;
}
