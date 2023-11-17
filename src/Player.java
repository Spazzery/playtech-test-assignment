import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class Player {

    private UUID playerId;
    private List<Transaction> transactions = new ArrayList<>();
    private Long balance = 0L;
    private List<Transaction> illegalTransactions = new ArrayList<>();

    public Player(UUID playerId) {
        this.playerId = playerId;
    }

    public void updateBalance(int amount) {
        balance += amount;
    }
}
