import lombok.Data;

import java.util.UUID;

@Data
public class Transaction {

    private UUID playerId;
    private ActionType actionType;
    private UUID matchId;  // can be null
    private Integer coins;
    private Side bettedSide;  // can be null

}
