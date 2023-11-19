import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {

        Casino casino = new Casino();

        // Read files and create objects
        List<Match> matches = Initializer.parseMatches("src/resources/match_data.txt");
        List<Player> players = Initializer.parsePlayers("src/resources/player_data.txt");
        List<Transaction> transactions = Initializer.parseActions("src/resources/player_data.txt");

        // Bind Transaction objects to Player
        Initializer.AddTransactionsToPlayers(players, transactions);

        // Separate legitimate players and illegitimate players and do calculations
        List<Player> legitimatePlayers = new ArrayList<>();
        List<Player> illegitimatePlayers = new ArrayList<>();

        for (Player player : players) {
            if (isLegitimatePlayer(player)) {
                legitimatePlayers.add(player);

                int casinoBetBalance = calculateCasinoBalance(player.getTransactions(), matches);
                casino.updateBalance(casinoBetBalance);

                int playerBalance = calculatePlayerBalance(player.getTransactions(), matches);
                player.updateBalance(playerBalance);

            }
            else {
                illegitimatePlayers.add(player);
            }
        }

        // Write the results into results file
        writeResults("src/result.txt", legitimatePlayers, illegitimatePlayers, casino.getBalance(), matches);

        // Let the user know the program has finished
        System.out.println("Finished the program!");

    }

    private static boolean isLegitimatePlayer(Player player) {
        long balance = 0;

        // Check player's each transaction to make sure they're all legit
        for (Transaction transaction : player.getTransactions()) {

            switch (transaction.getTransactionType()) {
                case DEPOSIT:
                    balance += transaction.getCoins();
                    break;
                case BET:
                    if (transaction.getCoins() > balance) {
                        player.getIllegalTransactions().add(transaction);
                        return false;
                    }
                    break;
                case WITHDRAW:
                    if (transaction.getCoins() > balance) {
                        player.getIllegalTransactions().add(transaction);
                        return false;
                    }
                    balance -= transaction.getCoins();
                    break;
            }
        }

        return true;
    }

    private static int calculateCasinoBalance(List<Transaction> transactions, List<Match> matches) {
        int casinoBalance = 0;

        for (Transaction transaction : transactions) {
            if (transaction.getTransactionType() == TransactionType.BET) {
                Match match = findMatchById(transaction.getMatchId(), matches);  // cant be null

                int profit = calculateProfit(transaction, match);  // positive number if player one, negative if lost
                casinoBalance -= profit;  // if player won, then casino lost
            }
        }

        return casinoBalance;
    }

    private static Match findMatchById(UUID matchId, List<Match> matches) {
        for (Match match : matches) {
            if (match.getMatchId().equals(matchId)) {
                return match;
            }
        }
        throw new RuntimeException("No existing match with ID: " + matchId.toString());
    }

    private static int calculateProfit(Transaction transaction, Match match) {
        Result betResult = match.getResult();
        Side bettedSide = transaction.getBettedSide();

        if (betResult == Result.DRAW) {  // If draw, then return nothing
            return 0;
        }

        float returnRate;
        if (betResult == Result.A && bettedSide == Side.A) {
            returnRate = match.getReturnRateForA();
        }
        else if (betResult == Result.B && bettedSide == Side.B) {
            returnRate = match.getReturnRateForB();
        }
        else {  // player lost
            return -transaction.getCoins();
        }

        BigDecimal coinsDecimal = BigDecimal.valueOf(transaction.getCoins())
                .multiply(BigDecimal.valueOf(returnRate))
                .setScale(0, RoundingMode.FLOOR);

        return coinsDecimal.intValue();
    }

    private static int calculatePlayerBalance(List<Transaction> transactions, List<Match> matches) {
        int playerBalance = 0;

        for (Transaction transaction : transactions) {
            if (transaction.getTransactionType() == TransactionType.BET) {

                Match match = findMatchById(transaction.getMatchId(), matches);  // cant be null

                int profit = calculateProfit(transaction, match);

                playerBalance += profit;
            }
            else if (transaction.getTransactionType() == TransactionType.DEPOSIT) {
                playerBalance += transaction.getCoins();
            }
            else if (transaction.getTransactionType() == TransactionType.WITHDRAW) {
                playerBalance -= transaction.getCoins();
            }
            else {
                throw new RuntimeException("No methods for transaction: " + transaction.getTransactionType());
            }
        }

        return playerBalance;
    }

    public static void writeResults(String outputPath, List<Player> legitimatePlayers, List<Player> illegitimatePlayers, long casinoBalance, List<Match> matches) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {

            // legitimate players
            for (Player legitimatePlayer : legitimatePlayers) {
                writer.write(legitimatePlayer.getPlayerId()
                        + " " + legitimatePlayer.getBalance()
                        + " " + calculateWinRate(legitimatePlayer, matches).toString().replace('.', ',')
                        + "\n");
            }
            writer.write("\n");

            // illegitimatePlayers
            for (Player illegitimatePlayer : illegitimatePlayers) {
                Transaction transaction = illegitimatePlayer.getIllegalTransactions().get(0);
                writer.write(illegitimatePlayer.getPlayerId()
                        + " " + transaction.getTransactionType()
                        + " " + transaction.getMatchId()
                        + " " + transaction.getCoins()
                        + " " + transaction.getBettedSide()
                        + "\n");
            }
            writer.write("\n");

            // casino host balance
            writer.write(String.valueOf(casinoBalance));

        } catch (IOException e) {
            throw new RuntimeException("Issue with writing into file: " + e);
        }
    }

    private static BigDecimal calculateWinRate(Player legitimatePlayer, List<Match> matches) {
        List<Transaction> transactions = legitimatePlayer.getTransactions();

        int wonGames = 0;
        int placedBets = 0;

        for (Transaction transaction : transactions) {
            if (transaction.getTransactionType() == TransactionType.BET) {
                placedBets++;
                Match match = findMatchById(transaction.getMatchId(), matches);
                if (match.getResult() == Result.A && transaction.getBettedSide() == Side.A) {
                    wonGames++;
                } else if (match.getResult() == Result.B && transaction.getBettedSide() == Side.B) {
                    wonGames++;
                }
            }
        }

        // Avoid division by zero
        if (placedBets == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal winRate = new BigDecimal(wonGames).divide(
                new BigDecimal(placedBets), 2, BigDecimal.ROUND_HALF_UP);

        return winRate;
    }
}
