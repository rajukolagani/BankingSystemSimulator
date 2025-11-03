import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final Bank bank = new Bank();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=== Banking System Simulator ===");
        boolean running = true;
        while (running) {
            try {
                showMainMenu();
                int choice = readInt();
                switch (choice) {
                    case 1 -> createAccountFlow();
                    case 2 -> performOperationsFlow();
                    case 3 -> {
                        System.out.println("Exiting... Goodbye.");
                        running = false;
                    }
                    default -> throw new InvalidChoiceException("Invalid main menu choice.");
                }
            } catch (InvalidChoiceException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        scanner.close();
    }

    private static void showMainMenu() {
        System.out.println("\nMain Menu:");
        System.out.println("1. Create an account");
        System.out.println("2. Perform operations on existing accounts");
        System.out.println("3. Exit");
        System.out.print("Choose an option: ");
    }

    private static void createAccountFlow() {
        try {
            System.out.print("Enter full name: ");
            String name = scanner.nextLine();
            String accNum = bank.createAccount(name);
            System.out.println("Account created. Account Number: " + accNum);
        } catch (InvalidNameException e) {
            System.out.println("Invalid name: " + e.getMessage());
        }
    }

    private static void performOperationsFlow() {
        System.out.print("Enter account number to operate on (or type 'list' to search/list): ");
        String input = scanner.nextLine().trim();
        try {
            if (input.equalsIgnoreCase("list")) {
                System.out.print("Enter partial name to search (blank for all): ");
                String p = scanner.nextLine();
                List<Account> results = bank.searchByName(p);
                if (results.isEmpty()) System.out.println("No accounts found.");
                else results.forEach(a -> System.out.println(a.getAccountNumber() + " - " + a.getHolderName() + " - Balance: " + a.getBalance()));
                return;
            }

            Account account = bank.getAccount(input);
            boolean back = false;
            while (!back) {
                showAccountMenu(account.getAccountNumber());
                int choice = readInt();
                switch (choice) {
                    case 1 -> depositFlow(account);
                    case 2 -> withdrawFlow(account);
                    case 3 -> transferFlow(account);
                    case 4 -> showBalanceFlow(account);
                    case 5 -> back = true;
                    case 6 -> runConcurrentDemo(account);
                    default -> System.out.println("Invalid choice.");
                }
            }
        } catch (AccountNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void showAccountMenu(String accNo) {
        System.out.println("\nAccount Menu (" + accNo + "):");
        System.out.println("1. Deposit");
        System.out.println("2. Withdraw");
        System.out.println("3. Transfer");
        System.out.println("4. Show balance");
        System.out.println("5. Return to main menu");
        System.out.println("6. Run concurrent transactions demo (multithreading)");
        System.out.print("Choice: ");
    }

    private static void depositFlow(Account a) {
        try {
            System.out.print("Enter deposit amount: ");
            double amt = readDouble();
            a.deposit(amt);
            System.out.println("Deposit successful. New balance: " + a.getBalance());
        } catch (InvalidAmountException e) {
            System.out.println("Invalid amount: " + e.getMessage());
        }
    }

    private static void withdrawFlow(Account a) {
        try {
            System.out.print("Enter withdrawal amount: ");
            double amt = readDouble();
            try {
                a.withdraw(amt);
                System.out.println("Withdrawal successful. New balance: " + a.getBalance());
            } catch (InsufficientBalanceException e) {
                System.out.println("Withdrawal failed: " + e.getMessage());
            } finally {
                System.out.println("Balance after attempted withdrawal: " + a.getBalance());
            }
        } catch (InvalidAmountException e) {
            System.out.println("Invalid amount: " + e.getMessage());
        }
    }

    private static void transferFlow(Account aFrom) {
        try {
            System.out.print("Enter destination account number: ");
            String dest = scanner.nextLine().trim();
            System.out.print("Enter transfer amount: ");
            double amt = readDouble();
            bank.transfer(aFrom.getAccountNumber(), dest, amt);
            System.out.println("Transfer successful.");
        } catch (InvalidAmountException | InsufficientBalanceException | AccountNotFoundException e) {
            System.out.println("Transfer failed: " + e.getMessage());
        }
    }

    private static void showBalanceFlow(Account a) {
        System.out.println("Account Number: " + a.getAccountNumber());
        System.out.println("Holder: " + a.getHolderName());
        System.out.println("Balance: " + a.getBalance());
    }
    private static void runConcurrentDemo(Account a) {
        System.out.println("Starting concurrent demo on account " + a.getAccountNumber());
        ExecutorService ex = Executors.newFixedThreadPool(4);
        
        ex.submit(new TransactionTask(bank, a.getAccountNumber(), 500, TransactionTask.Type.DEPOSIT));
        ex.submit(new TransactionTask(bank, a.getAccountNumber(), 200, TransactionTask.Type.WITHDRAW));
        ex.submit(new TransactionTask(bank, a.getAccountNumber(), 300, TransactionTask.Type.DEPOSIT));
        ex.submit(new TransactionTask(bank, a.getAccountNumber(), 700, TransactionTask.Type.WITHDRAW));
        ex.shutdown();
        try {
            if (!ex.awaitTermination(5, TimeUnit.SECONDS)) {
                ex.shutdownNow();
            }
        } catch (InterruptedException e) {
            ex.shutdownNow();
        }
        System.out.println("Concurrent demo finished. Final balance: " + a.getBalance());
    }
    private static int readInt() {
        while (true) {
            try {
                String s = scanner.nextLine();
                return Integer.parseInt(s.trim());
            } catch (NumberFormatException e) {
                System.out.print("Invalid integer. Try again: ");
            }
        }
    }

    private static double readDouble() {
        while (true) {
            try {
                String s = scanner.nextLine();
                return Double.parseDouble(s.trim());
            } catch (NumberFormatException e) {
                System.out.print("Invalid number. Try again: ");
            }
        }
    }
}
