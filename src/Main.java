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

        System.out.println(casino.getBalance());

        // Write the results into results file
        writeResults("src/result.txt", legitimatePlayers, illegitimatePlayers, casino.getBalance(), matches);

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

                int netChange = calculateNetChange(transaction, match);
                int profit = netChange - transaction.getCoins();
                if (profit > 0) {  // player won
                    casinoBalance -= (netChange + transaction.getCoins());  // casino loses
                }
                else if (profit < 0) {
                    casinoBalance += (netChange + transaction.getCoins());
                }
                // if DRAW, do nothing
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

    private static int calculateNetChange(Transaction transaction, Match match) {
        Result betResult = match.getResult();

        if (betResult == Result.DRAW) {  // If draw, then return nothing. None added to casino balance
            return 0;
        }

        float returnRate = (betResult == Result.A) ? match.getReturnRateForA() : match.getReturnRateForB();

        BigDecimal coinsDecimal = BigDecimal.valueOf(transaction.getCoins())
                .multiply(BigDecimal.valueOf(returnRate))
                .setScale(0, RoundingMode.FLOOR);

        return coinsDecimal.intValue();
    }

    private static int calculatePlayerBalance(List<Transaction> transactions, List<Match> matches) {
        int playerBalance = 0;

        for (Transaction transaction : transactions) {
            System.out.println("Initial: " + playerBalance);
            System.out.println(transaction.getCoins());

            if (transaction.getTransactionType() == TransactionType.BET) {
                Match match = findMatchById(transaction.getMatchId(), matches);  // cant be null

                int netChange = calculateNetChange(transaction, match);

                System.out.println("Netchange: " + netChange);
                System.out.println("Profit: " + (netChange - transaction.getCoins()));

                if (netChange != 0) {  // if == 0, then no need to remove and add the coins bet
                    playerBalance -= transaction.getCoins();  // the bet coins removed first
                }

                playerBalance += netChange;
            }
            else if (transaction.getTransactionType() == TransactionType.DEPOSIT) {
                playerBalance += transaction.getCoins();
                System.out.println("Deposit: " + transaction.getCoins());
            }
            else if (transaction.getTransactionType() == TransactionType.WITHDRAW) {
                playerBalance -= transaction.getCoins();
                System.out.println("Withdraw: " + transaction.getCoins());
            }
            else {
                throw new RuntimeException("No methods for transaction: " + transaction.getTransactionType());
            }

            System.out.println(playerBalance);
            System.out.println("------");
        }

        return playerBalance;
    }

    public static void writeResults(String outputPath, List<Player> legitimatePlayers, List<Player> illegitimatePlayers, long casinoBalance, List<Match> matches) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {

            // legitimate players
            for (Player legitimatePlayer : legitimatePlayers) {
                writer.write(legitimatePlayer.getPlayerId()
                        + " " + legitimatePlayer.getBalance()
                        + " " + calculateWinRate(legitimatePlayer, matches)
                        + "\n");
            }
            writer.write("\n");

            // illegitimatePlayers
            for (Player illegitimatePlayer : illegitimatePlayers) {
                System.out.println(illegitimatePlayer.getIllegalTransactions());
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
            throw new RuntimeException("Issue with writing into the file: " + e);
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

        BigDecimal winRate = new BigDecimal(wonGames).divide(new BigDecimal(placedBets), 2, BigDecimal.ROUND_HALF_UP);

        return winRate;
    }
}
