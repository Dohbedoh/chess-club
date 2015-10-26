package org.chesscorp.club.model;

import javax.persistence.*;

/**
 * Player data model. Can be used for actual players as well as robot players.
 *
 * @author Yannick Kirschhoffer <alcibiade@alcibiade.org>
 */
@Entity
public class EloRating {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(optional = false)
    private Player player;

    @ManyToOne(optional = false)
    private ChessGame chessGame;

    @Column(nullable = false)
    private Integer eloRating;

    public EloRating() {
    }

    public EloRating(Player player, ChessGame chessGame, Integer eloRating) {
        this.player = player;
        this.chessGame = chessGame;
        this.eloRating = eloRating;
    }

    public Long getId() {
        return id;
    }

    public Player getPlayer() {
        return player;
    }

    public ChessGame getChessGame() {
        return chessGame;
    }

    public Integer getEloRating() {
        return eloRating;
    }

    @Override
    public String toString() {
        return "EloRating{" +
                "id=" + id +
                ", player=" + player +
                ", chessGame=" + chessGame +
                ", eloRating=" + eloRating +
                '}';
    }
}