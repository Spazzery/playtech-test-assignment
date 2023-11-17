import lombok.Data;

@Data
public class Casino {
    private long balance;

    public Casino() {
        this.balance = 0L;
    }

    public void updateBalance(long amount) {
        this.balance += amount;
    }
}