package com.example.chess.service;

import com.example.chess.api.models.MoveDto;
import com.example.chess.engine.PieceMove;
import com.example.chess.engine.models.GamePlayerDesc;
import com.example.chess.exceptions.GameNotFoundException;
import com.example.chess.exceptions.InvalidTokenException;
import com.example.chess.repository.GameMetadata;
import com.example.chess.repository.GameRepository;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Consumer;

@Service
public class GameService {

    private final Logger log = LoggerFactory.getLogger(GameService.class);

    @Getter
    private final GameRepository gameRepository;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final SubmissionPublisher<GameMetadata> gameUpdatesPublisher = new SubmissionPublisher<>(executor, Integer.MAX_VALUE);
    private final ConcurrentMap<GamePlayerDesc, Consumer<GameMetadata>> handlers = new ConcurrentHashMap<>();

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;

        GameUpdatesSubscriber subscriber = new GameUpdatesSubscriber();
        gameUpdatesPublisher.subscribe(subscriber);
    }

    public GameMetadata newGame() {
        return gameRepository.newGame();
    }

    public GameMetadata find(String id) {
        return gameRepository.find(id).orElseThrow(() -> new GameNotFoundException("Game not found: " + id));
    }

    public GameMetadata joinGame(String id) {
        var metadata = find(id);
        metadata.setSecondPlayerJoined(true);
        gameRepository.save(metadata);
        gameUpdatesPublisher.submit(metadata);
        return metadata;
    }

    public GameMetadata applyMove(String id, String token, MoveDto moveDto) {
        var metadata = find(id);
        if (!metadata.currentPlayerToken().equals(token)) {
            throw new InvalidTokenException("Invalid token");
        }
        metadata.applyMove(PieceMove.of(moveDto));
        gameRepository.save(metadata);

        log.debug("Submit called {} {} {}", id, token, moveDto);
        gameUpdatesPublisher.submit(metadata);
        return metadata;
    }

    public void awaitOpponentMove(GamePlayerDesc gamePlayerDesc, Consumer<GameMetadata> handler) {
        handlers.put(gamePlayerDesc, handler);
    }

    class GameUpdatesSubscriber implements Flow.Subscriber<GameMetadata> {

        private Flow.Subscription subscription;

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            subscription.request(1);
        }

        @Override
        public void onNext(GameMetadata metadata) {
            log.debug("On next {} {} {}", metadata.getId(), metadata.currentPlayerToken(), metadata.getGame().getCurrentPlayer());

            GamePlayerDesc gamePlayerDesc = metadata.currentPlayerDesc();
            Optional.ofNullable(handlers.remove(gamePlayerDesc)).ifPresent(handler -> {
                log.debug("Handlers present {} {} {}", metadata.getId(), metadata.currentPlayerToken(), metadata.getGame().getCurrentPlayer());
                handler.accept(metadata);
            });

            subscription.request(1);
        }

        @Override
        public void onError(Throwable throwable) {
            log.error(throwable.getMessage(), throwable);
        }

        @Override
        public void onComplete() {

        }
    }
}
