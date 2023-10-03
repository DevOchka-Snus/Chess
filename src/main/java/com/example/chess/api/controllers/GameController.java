package com.example.chess.api.controllers;

import com.example.chess.api.models.*;
import com.example.chess.engine.models.GamePlayerDesc;
import com.example.chess.engine.models.PieceColor;
import com.example.chess.exceptions.GameNotFoundException;
import com.example.chess.exceptions.InvalidMoveException;
import com.example.chess.exceptions.InvalidTokenException;
import com.example.chess.repository.GameMetadata;
import com.example.chess.service.GameService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameController {
    private final Logger log = LoggerFactory.getLogger(GameController.class);
    private final GameService service;

    @GetMapping("/sample")
    public GameStateDto gameStateDto() {
        GameStateDto gameStateDto = new GameStateDto();
        gameStateDto.setCurrentPlayer(ColorDto.WHITE);
        PieceDto pawn1 = new PieceDto();
        pawn1.setColor(ColorDto.WHITE);
        pawn1.setPieceType(PieceTypeDto.PAWN);
        pawn1.setPosition("e2");
        pawn1.setValidMoves(Arrays.asList("e3", "e4"));

        PieceDto pawnBlack = new PieceDto();
        pawnBlack.setColor(ColorDto.BLACK);
        pawnBlack.setPieceType(PieceTypeDto.PAWN);
        pawnBlack.setPosition("f7");

        gameStateDto.setPieces(Arrays.asList(pawn1, pawnBlack));

        return gameStateDto;
    }

    @PostMapping("/{id}/move")
    public ResponseEntity<?> makeMove(@RequestHeader("ptoken") String playerToken,
                                      @PathVariable("id") String id,
                                      @RequestBody MoveDto moveDto) {
        log.debug("move {} {} {}", id, playerToken, moveDto);

        GameMetadata metadata = service.applyMove(id, playerToken, moveDto);
        if (metadata.getGame().isFinished()) {
            log.info("game finished {}", metadata.getGame().getStatus());
        }
        return ResponseEntity.ok(metadata.getGameStateDtoForPlayer(playerToken));
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<?> join(@PathVariable("id") String id) {
        log.debug("join {} ", id);

        GameMetadata metadata = service.joinGame(id);
        String blackPlayerToken = metadata.getPlayerTokens().get(PieceColor.BLACK);
        return ResponseEntity.ok(new GameConnectionParamsDto(id, blackPlayerToken,
                ColorDto.BLACK, metadata.getGameStateDtoForPlayer(blackPlayerToken)));
    }

    @PostMapping("/host")
    public ResponseEntity<?> host() {
        GameMetadata metadata = service.newGame();
        String whitePlayerToken = metadata.getPlayerTokens().get(PieceColor.WHITE);

        log.debug("host {} {}", metadata.getId(), whitePlayerToken);
        return ResponseEntity.ok(new GameConnectionParamsDto(metadata.getId(), whitePlayerToken,
                ColorDto.WHITE, metadata.getGameStateDtoForPlayer(whitePlayerToken)));
    }

    @PostMapping("/{id}/wait-for-my-move")
    public DeferredResult<?> waitForMove(@RequestHeader("ptoken") String playerToken,
                                         @PathVariable("id") String id) {
        log.debug("await move {} {}", id, playerToken);

        DeferredResult<Object> deferredResult = new DeferredResult<>(TimeUnit.MINUTES.toMillis(60));
        GameMetadata metadata = service.find(id);

        deferredResult.onTimeout(() -> {
            deferredResult.setResult(new ResponseEntity<String>(HttpStatus.REQUEST_TIMEOUT));
            log.warn("await move timeout {} {}", id, playerToken);
        });

        if (metadata.currentPlayerToken().equals(playerToken)
                && metadata.isSecondPlayerJoined()) {
            deferredResult.setResult(metadata.getGameStateDtoForPlayer(playerToken));
            log.debug("await move immediate response {} {}", id, playerToken);
        } else {
            service.awaitOpponentMove(new GamePlayerDesc(id, playerToken), m -> {
                deferredResult.setResult(m.getGameStateDtoForPlayer(playerToken));
                log.debug("await move completed {} {}", id, playerToken);
            });
        }

        return deferredResult;
    }

    @ExceptionHandler(GameNotFoundException.class)
    public ResponseEntity<?> handleNotFound() {
        return new ResponseEntity<Object>("game not found", new HttpHeaders(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidMoveException.class)
    public ResponseEntity<?> handleInvalidMove() {
        return new ResponseEntity<Object>("bad move", new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<?> handleInvalidToken() {
        return new ResponseEntity<Object>("bad token", new HttpHeaders(), HttpStatus.FORBIDDEN);
    }
}
