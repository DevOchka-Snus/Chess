package com.example.chess.repository;

import com.example.chess.api.models.GameStateDto;
import com.example.chess.engine.ChessGame;
import com.example.chess.engine.models.PieceMove;
import com.example.chess.engine.models.GamePlayerDesc;
import com.example.chess.engine.models.PieceColor;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Data
public class GameMetadata {
    private String id;
    private Map<PieceColor, String> playerTokens = generateTokens();
    private ChessGame game;
    private OffsetDateTime dateOfCreating = OffsetDateTime.now();
    private boolean secondPlayerJoined = false;
    private Map<PieceColor, String> generateTokens() {
        return Map.of(
                PieceColor.WHITE, UUID.randomUUID().toString(),
                PieceColor.BLACK, UUID.randomUUID().toString()
        );
    }
    public boolean isSecondPlayerJoined() {
        return secondPlayerJoined;
    }

    public String currentPlayerToken() {
        return playerTokens.get(game.getCurrentPlayer().negate());
    }

    public GamePlayerDesc currentPlayerDesc() {
        return new GamePlayerDesc(id, currentPlayerToken());
    }

    public void applyMove(PieceMove pieceMove) {
        game = game.applyMove(pieceMove);
    }

    public GameStateDto getGameStateDtoForPlayer(String token) {
        boolean forCurrentPlayer = currentPlayerToken().equals(token);

        GameStateDto gameStateDto = new GameStateDto();
        gameStateDto.setMyTurn(forCurrentPlayer);
        gameStateDto.setCurrentPlayer(game.getCurrentPlayer().toDto());

        gameStateDto.setPieces(game.calculatePieceDtos(forCurrentPlayer));
        if (!game.getPreviousMoves().isEmpty()) {
            gameStateDto.setLastOpponentMove(
                    game.getPreviousMoves().get(game.getPreviousMoves().size() - 1).toDto()
            );
        }

        switch (game.getStatus()) {
            case CHECK -> gameStateDto.setCheck(true);
            case CHECKMATE -> {
                gameStateDto.setGameFinished(true);
                gameStateDto.setGameFinishedReason("Checkmate! " + game.getCurrentPlayer().negate() + " wins!");
                gameStateDto.setWinner(game.getCurrentPlayer().negate().toDto());
            }
            case DRAW_STALEMATE -> {
                gameStateDto.setGameFinished(true);
                gameStateDto.setGameFinishedReason("Game finished with a stalemate deaw!");
            }
            case NORMAL -> {}
        }

        return gameStateDto;
    }
}
