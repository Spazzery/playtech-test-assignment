import lombok.Data;

import java.util.UUID;

@Data
public class Transaction {

    private UUID playerId;
    private TransactionType transactionType;
    private UUID matchId;  // can be null
    private Integer coins;
    private Side bettedSide;  // can be null

}
