import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Bank {
    private final Map<String, Account> accounts = new ConcurrentHashMap<>();
    private final Random random = new Random();

    // Creates account given full name, return account number
    public String createAccount(String fullName) throws InvalidNameException {
        if (fullName == null || fullName.trim().isEmpty())
            throw new InvalidNameException("Name cannot be empty.");
        String initials = extractInitials(fullName);
        String accNum;
        do {
            int rnd = 1000 + random.nextInt(9000);
            accNum = initials + rnd;
        } while (accounts.containsKey(accNum));
        Account acc = new Account(accNum, fullName.trim());
        accounts.put(accNum, acc);
        return accNum;
    }

    public Account getAccount(String accountNumber) throws AccountNotFoundException {
        Account a = accounts.get(accountNumber);
        if (a == null) throw new AccountNotFoundException("Account not found: " + accountNumber);
        return a;
    }

    public void transfer(String fromAcc, String toAcc, double amount)
            throws AccountNotFoundException, InvalidAmountException, InsufficientBalanceException {
        if (amount <= 0) throw new InvalidAmountException("Transfer amount must be positive.");
        Account aFrom = getAccount(fromAcc);
        Account aTo = getAccount(toAcc);
        Account firstLock = aFrom.getAccountNumber().compareTo(aTo.getAccountNumber()) <= 0 ? aFrom : aTo;
        Account secondLock = firstLock == aFrom ? aTo : aFrom;

        synchronized (firstLock) {
            synchronized (secondLock) {
                // withdraw
                aFrom.withdraw(amount);
                // deposit
                aTo.deposit(amount);
            }
        }
    }

    public List<Account> searchByName(String partialName) {
        String p = partialName == null ? "" : partialName.trim().toLowerCase();
        return accounts.values().stream()
                .filter(a -> a.getHolderName().toLowerCase().contains(p))
                .collect(Collectors.toList());
    }

    public Collection<Account> getAllAccounts() {
        return Collections.unmodifiableCollection(accounts.values());
    }

    private String extractInitials(String name) {
        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isEmpty()) sb.append(Character.toUpperCase(p.charAt(0)));
            if (sb.length() == 2) break; 
        }
        while (sb.length() < 2) sb.append('X');
        return sb.toString();
    }
}
