import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class Player {

    private UUID playerId;
    private List<Transaction> transactions;
    private Long balance;

    public Player(UUID playerId) {
        this.playerId = playerId;
    }

//    public static List<Player> convertPlayers(List<String> playerLines, List<Match> matches) {
//
//        List<Player> players = new ArrayList<>();
//        for (String playerLine : playerLines) {
//            String[] parts = playerLine.split(",");
//
//            UUID playerId = UUID.fromString(parts[0]);
//            ActionType action = ActionType.valueOf(parts[1]);
//            UUID matchId = !parts[2].isEmpty() ? UUID.fromString(parts[2]) : null;
//            Long balance = !parts[3].isEmpty() ? Long.parseLong(parts[3]) : null;
//            Result result = !parts[4].isEmpty() ? Result.valueOf(parts[4]) : null;
//
//            Optional<Match> match;
//            if (matchId != null) {
//                match = matches.stream()
//                        .filter(aMatch -> aMatch.getMatchId().equals(matchId))
//                        .findFirst();
//            }
//
//            Optional<Player> existingPlayer = players.stream()
//                    .filter(player -> player.getPlayerId().equals(playerId))
//                    .findFirst();
//
//            if (existingPlayer.isPresent()) {
//                Player player = existingPlayer.get();
//
//                player.getAction().add(action);
//
//            }
//            else {
//                Player player = new Player();
//
//                player.setPlayerId(playerId);
//                player.setAction(new ArrayList<>(List.of(action)));
//                player.setMatchId(matchId);
//                player.setBalance(balance);
//                player.setResult(result);
//
//                players.add(player);
//            }
//
//        }
//
//        return players;
//
//    }
}
