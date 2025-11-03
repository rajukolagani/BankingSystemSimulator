public class CurrentAccount extends Account {

    public CurrentAccount(String accountNumber, String holderName, double balance) {
        super(accountNumber, holderName);
        try {
            if (balance > 0)
                deposit(balance);
        } catch (InvalidAmountException e) {
            System.out.println("Invalid initial deposit: " + e.getMessage());
        }
    }

    @Override
    public synchronized void withdraw(double amount)
            throws InvalidAmountException, InsufficientBalanceException {
        if (amount > getBalance() + 5000)
            throw new InsufficientBalanceException("Overdraft limit exceeded (max 5000).");
        super.withdraw(amount);
    }
}
