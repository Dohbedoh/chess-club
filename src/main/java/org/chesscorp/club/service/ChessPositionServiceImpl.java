package org.chesscorp.club.service;

import org.alcibiade.chess.model.ChessMovePath;
import org.alcibiade.chess.model.ChessPosition;
import org.alcibiade.chess.persistence.PgnMarshaller;
import org.alcibiade.chess.persistence.PositionMarshaller;
import org.alcibiade.chess.rules.ChessHelper;
import org.alcibiade.chess.rules.ChessRules;
import org.chesscorp.club.model.game.ChessGame;
import org.chesscorp.club.model.game.ChessMove;
import org.chesscorp.club.model.stats.ChessClubPosition;
import org.chesscorp.club.model.stats.ChessMoveToPosition;
import org.chesscorp.club.persistence.ChessGameRepository;
import org.chesscorp.club.persistence.ChessMoveRepository;
import org.chesscorp.club.persistence.ChessMoveToPositionRepository;
import org.chesscorp.club.persistence.ChessPositionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Position management implementations.
 */
@Component
public class ChessPositionServiceImpl implements ChessPositionService {
    private static final List<ChessGame> EMPTY_GAMES_LIST = new ArrayList<>();
    private Logger logger = LoggerFactory.getLogger(ChessPositionServiceImpl.class);
    @Autowired
    private ChessPositionRepository chessPositionRepository;
    @Autowired
    private ChessGameRepository chessGameRepository;
    @Autowired
    private ChessMoveToPositionRepository chessMoveToPositionRepository;
    @Autowired
    private ChessMoveRepository chessMoveRepository;
    @Autowired
    private ChessRules chessRules;
    @Autowired
    private PgnMarshaller pgnMarshaller;
    @Autowired
    private PositionMarshaller positionMarshaller;

    @Override
    @Transactional
    public void updateMovePositions() {
        logger.debug("Updating position tables");

        ChessMoveToPosition lastProcessedMove = chessMoveToPositionRepository.findFirstByOrderByChessMoveIdDesc();
        logger.debug("Last processed move is {}", lastProcessedMove);

        long lastMoveId = 0;

        if (lastProcessedMove != null) {
            lastMoveId = lastProcessedMove.getChessMoveId();
        }

        Map<Long, ChessPosition> positionCache = new HashMap<>();

        List<ChessMove> movesToProcess = chessMoveRepository.findAllByIdGreaterThan(lastMoveId);

        movesToProcess.stream().forEach(m -> {
            ChessGame game = m.getGame();

            ChessPosition position = chessRules.getInitialPosition();
            for (ChessMove move : game.getMoves()) {
                ChessPosition cachedPosition = positionCache.get(move.getId());

                if (cachedPosition == null) {
                    ChessMovePath path = pgnMarshaller.convertPgnToMove(position, move.getPgn());
                    position = ChessHelper.applyMoveAndSwitch(chessRules, position, path);
                    positionCache.put(move.getId(), position);
                } else {
                    position = cachedPosition;
                }

                if (move.getId().equals(m.getId())) {
                    break;
                }
            }

            String positionText = positionMarshaller.convertPositionToString(position);
            logger.trace("Processing move {} - {}", m.getId(), positionText);

            ChessClubPosition clubPosition = chessPositionRepository.findOneByText(positionText);
            if (clubPosition == null) {
                clubPosition = chessPositionRepository.save(new ChessClubPosition(positionText));
            }

            chessMoveToPositionRepository.save(new ChessMoveToPosition(m.getId(), clubPosition));
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChessGame> findRelatedGames(Number gameId) {
        ChessGame game = chessGameRepository.findOne(gameId.longValue());
        if (game == null) {
            return EMPTY_GAMES_LIST;
        }

        List<ChessMove> moves = game.getMoves();
        if (moves.isEmpty()) {
            return EMPTY_GAMES_LIST;
        }

        List<ChessGame> relatedGames = new ArrayList<>();
        ChessMove lastMove = moves.get(moves.size() - 1);
        ChessMoveToPosition moveToPosition = chessMoveToPositionRepository.getOne(lastMove.getId());
        ChessClubPosition position = moveToPosition.getChessPosition();

        chessMoveToPositionRepository
                .findFirst10ByChessClubPositionId(position.getId()).stream()
                .map(mtp -> chessMoveRepository.getOne(mtp.getChessMoveId()))
                .filter(move -> !move.getGame().getId().equals(game.getId()))
                .map(ChessMove::getGame)
                .forEach(relatedGames::add);

        return relatedGames;
    }
}