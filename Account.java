import java.util.Objects;

public class Account {
    private final String accountNumber;
    private final String holderName;
    protected double balance; // made protected for subclass access

    // ✅ Constructor with default balance = 0.0
    public Account(String accountNumber, String holderName) {
        this(accountNumber, holderName, 0.0);
    }

    // ✅ New constructor with custom initial balance
    public Account(String accountNumber, String holderName, double balance) {
        this.accountNumber = Objects.requireNonNull(accountNumber, "Account number cannot be null");
        this.holderName = Objects.requireNonNull(holderName, "Holder name cannot be null");
        if (balance < 0) throw new IllegalArgumentException("Initial balance cannot be negative.");
        this.balance = balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getHolderName() {
        return holderName;
    }

    public synchronized void deposit(double amount) throws InvalidAmountException {
        if (amount <= 0) throw new InvalidAmountException("Deposit amount must be positive.");
        balance += amount;
    }

    public synchronized void withdraw(double amount) throws InvalidAmountException, InsufficientBalanceException {
        if (amount <= 0) throw new InvalidAmountException("Withdrawal amount must be positive.");
        if (amount > balance) throw new InsufficientBalanceException("Insufficient balance.");
        balance -= amount;
    }

    public synchronized double getBalance() {
        return balance;
    }

    @Override
    public String toString() {
        return "Account{" +
                "accountNumber='" + accountNumber + '\'' +
                ", holderName='" + holderName + '\'' +
                ", balance=" + balance +
                '}';
    }
}
