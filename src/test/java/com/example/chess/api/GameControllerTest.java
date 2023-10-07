package com.example.chess.api;

import com.example.chess.api.models.GameConnectionParamsDto;
import com.example.chess.api.models.GameStateDto;
import com.example.chess.api.models.MoveDto;
import com.example.chess.engine.ChessGameTest;
import com.example.chess.engine.models.PieceMove;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GameControllerTest {
    private static final int TIMEOUT = 500;

    ExecutorService cfPool = Executors.newFixedThreadPool(12, r -> new Thread(r, "completable future pool"));

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @Disabled
    void childrenMate() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(4, r -> new Thread(r, "run pool #" + counter.getAndIncrement()));

        for (int i = 0; i < 100; i++) {
            pool.submit(() -> {
                try {
                    testChildrenMate();
                } catch (ExecutionException | InterruptedException | TimeoutException e) {
                    e.printStackTrace();
                }
            });
        }

        Thread.sleep(100_000);
    }

    @RepeatedTest(10)
    void testChildrenMate() throws ExecutionException, InterruptedException, TimeoutException {
        List<MoveDto> whiteMoves = ChessGameTest.movesFromString("E2E4 D1H5 F1C4 H5F7")
                .map(PieceMove::toDto).collect(Collectors.toList());
        List<MoveDto> blackMoves = ChessGameTest.movesFromString("E7E5 B8C6 G8F6")
                .map(PieceMove::toDto).collect(Collectors.toList());

        GameConnectionParamsDto white = host();
        getQuietly(awaitMove(white.getId(), white.getToken()), 1);

        join(host().getId());
        join(host().getId());

        GameConnectionParamsDto black = join(white.getId());

        CompletableFuture<GameStateDto> whiteMoveSeq = CompletableFuture.supplyAsync(() ->
                moveSequence(white.getId(), white.getToken(), whiteMoves), cfPool);
        CompletableFuture<GameStateDto> blackMoveSeq = CompletableFuture.supplyAsync(() ->
                moveSequence(black.getId(), black.getToken(), blackMoves), cfPool);

        GameStateDto whiteFinal = whiteMoveSeq.get(TIMEOUT * 2, TimeUnit.SECONDS);
        GameStateDto blackFinal = blackMoveSeq.get(TIMEOUT * 2, TimeUnit.SECONDS);

        assertTrue(whiteFinal.isGameFinished());
        assertTrue(blackFinal.isGameFinished());

        assertEquals("Checkmate! WHITE wins!", whiteFinal.getGameFinishedReason());
        assertEquals("Checkmate! WHITE wins!", blackFinal.getGameFinishedReason());
    }

    private GameStateDto moveSequence(String id, String token, List<MoveDto> moves) {
        GameStateDto state = null;
        for (MoveDto move : moves) {
            try {
                state = awaitMove(id, token).get(TIMEOUT, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
                throw new AssertionFailedError(e.getMessage());
            }
            assertTrue(state.isMyTurn());
            state = move(id, token, move);
            assertFalse(state.isMyTurn());
        }

        if (state != null && !state.isGameFinished()) {
            try {
                state = awaitMove(id, token).get(TIMEOUT, TimeUnit.SECONDS);
            }catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new AssertionFailedError(e.getMessage());
            }
        }
        return state;
    }

    private GameStateDto move(String id, String pheader, MoveDto move) {
        return post("/" + id + "/move", Optional.of(pheader), move, GameStateDto.class);
    }

    private GameConnectionParamsDto join(String id) {
        return post("/" + id + "/join", Optional.empty(), "", GameConnectionParamsDto.class);
    }

    private <T> T getQuietly(CompletableFuture<T> completableFuture, long sec) {
        try {
            return completableFuture.get(sec, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return null;
        }
    }

    private CompletableFuture<GameStateDto> awaitMove(String id, String pheader) {
        return CompletableFuture.supplyAsync(() ->
                post("/" + id + "/wait-for-my-move", Optional.of(pheader), "", GameStateDto.class), cfPool);
    }

    private GameConnectionParamsDto host() {
        return post("/host", Optional.empty(), "", GameConnectionParamsDto.class);
    }

    private <T, R> T post(String subUrl, Optional<String> pheader, R body, Class<T> tClass) {
        HttpHeaders httpHeaders = new HttpHeaders();
        pheader.ifPresent(h -> {
            httpHeaders.add("ptoken", h);
        });

        HttpEntity<R> request = new HttpEntity<>(body, httpHeaders);

        ResponseEntity<T> responseEntity =
                restTemplate.postForEntity("http://localhost:" + port + "/api/game/" + subUrl, request, tClass);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        return responseEntity.getBody();
    }

    @Test
    public void testGameNotFoundJoin() {
        ResponseEntity<String> badJoin =
                restTemplate.postForEntity("http://localhost:" + port + "/api/game/broken-id/join", "", String.class);
        assertEquals(HttpStatus.NOT_FOUND, badJoin.getStatusCode());
        assertNotNull(badJoin.getBody());
        assertTrue(badJoin.getBody().contains("not found"));
    }

    @Test
    public void testGameNotFoundWait() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("ptoken", "broken-token");
        HttpEntity<String> request = new HttpEntity<>("", httpHeaders);

        ResponseEntity<String> badJoin =
                restTemplate.postForEntity("http://localhost:" + port + "/api/game/broken-id//wait-for-my-move", "", String.class);
        assertEquals(HttpStatus.NOT_FOUND, badJoin.getStatusCode());
        assertNotNull(badJoin.getBody());
        assertTrue(badJoin.getBody().contains("not found"));
    }
}
