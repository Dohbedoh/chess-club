package org.chesscorp.club.service;

import org.chesscorp.club.model.Player;
import org.chesscorp.club.persistence.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PlayerServiceImpl implements PlayerService {
    private Logger logger = LoggerFactory.getLogger(PlayerServiceImpl.class);

    @Autowired
    private PlayerRepository playerRepository;


    @Override
    public List<Player> search(String query) {
        String processedQuery = query.toLowerCase();
        List<Player> result = new ArrayList<>();

        /*
         * Yes, this is a quite lame approach, and let's find some time for an index-based approach soon !
         */
        for (Player player : playerRepository.findAll()) {
            String displayName = player.getDisplayName().toLowerCase();

            if (displayName.contains(query)) {
                result.add(player);
            }
        }

        return result;
    }
}
