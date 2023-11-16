import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class Match {

    private UUID matchId;
    private float returnRateForA;
    private float returnRateForB;
    private Result result;

}
