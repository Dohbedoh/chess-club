package org.chesscorp.club.persistence;

import org.chesscorp.club.model.ChessGame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface ChessGameRepository extends JpaRepository<ChessGame, Number> {

    List<ChessGame> findByWhitePlayerIdOrBlackPlayerId(Number whitePlayerId, Number blackPlayerId);

    List<ChessGame> findByWhitePlayerIdAndBlackPlayerIdAndStartDate(Number whitePlayerId, Number blackPlayerId, Date startDate);
}
