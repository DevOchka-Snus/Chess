package com.example.chess.repository;

import com.example.chess.engine.ChessGame;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class GameRepositoryImpl implements GameRepository{
    private Map<String, GameMetadata> metadataMap = new ConcurrentHashMap<>();
    @Override
    public GameMetadata newGame() {
        ChessGame game = ChessGame.start();
        GameMetadata metadata  = new GameMetadata();
        metadata.setGame(game);
        metadata.setId(generateId());

        metadataMap.put(metadata.getId(), metadata);
        return metadata;
    }

    private String generateId() {
        String id;
        do {
            id = UUID.randomUUID().toString();
        } while (find(id).isPresent());

        return id;
    }

    @Override
    public Optional<GameMetadata> find(String id) {
        return Optional.ofNullable(metadataMap.get(id));
    }

    @Override
    public void save(GameMetadata metadata) {
        metadataMap.put(metadata.getId(), metadata);
    }
}
