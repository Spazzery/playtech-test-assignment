import java.util.List;

public class Main {
    public static void main(String[] args) {

        // Read files and create objects
        List<Match> matches = Initializer.parseMatches("resources/match_data.txt");
        List<Player> players = Initializer.parsePlayers("resources/player_data.txt");
        List<Transaction> transactions = Initializer.parseActions("resources/player_data.txt");

        // Bind Transaction objects to Player
        Initializer.AddTransactionsToPlayers(players, transactions);




        // Step 4: Process Match Results
        // ...

        // Step 5: Calculate Casino Host Balance
        // ...

        // Step 6: Identify Legitimate and Illegitimate Players
        // ...

        // Step 7: Calculate Win Rate
        // ...

        // Step 8: Write Results to "result.txt"
        // ...


    }
}
