import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Bank {
    private final Map<String, Account> accounts = new ConcurrentHashMap<>();
    private int nextAccountNumber = 1001;

    // Generate unique account number
    public synchronized String generateAccountNumber() {
        return "ACC" + (nextAccountNumber++);
    }

    // Add new account
    public synchronized void addAccount(Account account) {
        accounts.put(account.getAccountNumber(), account);
    }

    // Get account by number
    public Account getAccount(String accountNumber) throws AccountNotFoundException {
        Account acc = accounts.get(accountNumber);
        if (acc == null)
            throw new AccountNotFoundException("Account not found: " + accountNumber);
        return acc;
    }

    // Search accounts by holder name (using streams)
    public List<Account> searchByName(String partial) {
        String lower = partial.toLowerCase();
        return accounts.values().stream()
                .filter(a -> a.getHolderName().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    // Transfer between accounts
    public synchronized void transfer(String fromAccNo, String toAccNo, double amount)
            throws InvalidAmountException, InsufficientBalanceException, AccountNotFoundException {

        if (amount <= 0)
            throw new InvalidAmountException("Transfer amount must be positive.");

        Account from = getAccount(fromAccNo);
        Account to = getAccount(toAccNo);

        from.withdraw(amount);
        to.deposit(amount);
    }

    // Get all accounts (for listing)
    public Collection<Account> getAllAccounts() {
        return accounts.values();
    }
}
