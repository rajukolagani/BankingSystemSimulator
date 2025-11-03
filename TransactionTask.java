// Demonstrates operations on an account.
// Use to show deposits or withdrawals concurrently.
public class TransactionTask implements Runnable {
    public enum Type { DEPOSIT, WITHDRAW }
    private final Bank bank;
    private final String accNumber;
    private final double amount;
    private final Type type;

    public TransactionTask(Bank bank, String accNumber, double amount, Type type) {
        this.bank = bank;
        this.accNumber = accNumber;
        this.amount = amount;
        this.type = type;
    }

    @Override
    public void run() {
        try {
            Account a = bank.getAccount(accNumber);
            switch (type) {
                case DEPOSIT:
                    a.deposit(amount);
                    System.out.println(Thread.currentThread().getName() + " deposited " + amount + " to " + accNumber);
                    break;
                case WITHDRAW:
                    try {
                        a.withdraw(amount);
                        System.out.println(Thread.currentThread().getName() + " withdrew " + amount + " from " + accNumber);
                    } catch (InsufficientBalanceException e) {
                        System.out.println(Thread.currentThread().getName() + " failed withdraw: " + e.getMessage());
                    }
                    break;
            }
        } catch (AccountNotFoundException | InvalidAmountException e) {
            System.out.println(Thread.currentThread().getName() + " error: " + e.getMessage());
        }
    }
}
