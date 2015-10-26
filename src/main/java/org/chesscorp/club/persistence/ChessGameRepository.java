package org.chesscorp.club.persistence;

import org.alcibiade.chess.model.ChessGameStatus;
import org.chesscorp.club.model.ChessGame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface ChessGameRepository extends JpaRepository<ChessGame, Number> {

    List<ChessGame> findByWhitePlayerIdAndStatusInOrBlackPlayerIdAndStatusIn(
            Number whitePlayerId, List<ChessGameStatus> whiteStatus, Number blackPlayerId, List<ChessGameStatus> blackStatus);

    List<ChessGame> findByWhitePlayerIdAndBlackPlayerIdAndStartDate(Number whitePlayerId, Number blackPlayerId, Date startDate);
}
