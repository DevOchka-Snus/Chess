package com.example.chess.repository;

import java.util.Optional;

public interface GameRepository {
    GameMetadata newGame();
    Optional<GameMetadata> find(String id);
    void save(GameMetadata metadata);
}
