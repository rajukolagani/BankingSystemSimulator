public class SavingsAccount extends Account {

    public SavingsAccount(String accountNumber, String holderName, double balance) {
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
        if (amount > getBalance() - 100)
            throw new InsufficientBalanceException("Minimum balance of 100 must be maintained.");
        super.withdraw(amount);
    }
}
