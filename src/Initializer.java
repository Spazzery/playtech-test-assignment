import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Initializer {

    public static List<Match> parseMatches(String path) {
        List<String> matchLines = FileReaderHelper.readAllLines(path);

        List<Match> matches = new ArrayList<>();
        for (String matchLine : matchLines) {
            String[] parts = matchLine.split(",");

            UUID id = UUID.fromString(parts[0]);
            float returnRateForA = Float.parseFloat(parts[1]);
            float returnRateForB = Float.parseFloat(parts[2]);
            Result result = Result.valueOf(parts[3]);

            Match match = new Match();
            match.setMatchId(id);
            match.setReturnRateForA(returnRateForA);
            match.setReturnRateForB(returnRateForB);
            match.setResult(result);

            matches.add(match);
        }

        return matches;
    }

    public static List<Player> parsePlayers(String path) {
        List<String> playerLines = FileReaderHelper.readAllLines(path);

        List<Player> players = new ArrayList<>();
        for (String playerLine : playerLines) {
            String[] parts = playerLine.split(",");

            UUID playerId = UUID.fromString(parts[0]);

            if (!containsPlayerId(players, playerId)) {
                Player player = new Player(playerId);
                player.setBalance(0L);
                players.add(player);
            }
        }
        return players;
    }
    private static boolean containsPlayerId(List<Player> players, UUID playerId) {
        for (Player player : players) {
            if (player.getPlayerId().equals(playerId)) {
                return true;
            }
        }
        return false;
    }

    public static List<Transaction> parseActions(String path) {
        List<String> actionLines = FileReaderHelper.readAllLines(path);

        List<Transaction> transactions = new ArrayList<>();
        for (String actionLine : actionLines) {
            String[] parts = actionLine.split(",");

            UUID playerId = UUID.fromString(parts[0]);
            TransactionType transactionType = TransactionType.valueOf(parts[1]);
            UUID matchId = !parts[2].isEmpty() ? UUID.fromString(parts[2]) : null;
            Integer coins = Integer.parseInt(parts[3]);
            Side bettedSide = parts.length > 4 ? Side.valueOf(parts[4]) : null;

            Transaction transaction = new Transaction();
            transaction.setPlayerId(playerId);
            transaction.setTransactionType(transactionType);
            transaction.setCoins(coins);

            // if BET can be certain then that matchId and bettedSide are not empty
            if (transactionType == TransactionType.BET) {
                transaction.setMatchId(matchId);
                transaction.setBettedSide(bettedSide);
            }

            transactions.add(transaction);
        }

        return transactions;
    }

    public static void AddTransactionsToPlayers(List<Player> players, List<Transaction> transactions) {
        for (Player player : players) {

            List<Transaction> playerTransactions = new ArrayList<>();
            for (Transaction transaction : transactions) {
                if (transaction.getPlayerId().equals(player.getPlayerId())) {
                    playerTransactions.add(transaction);
                }
            }

            player.setTransactions(playerTransactions);
        }
    }
}
