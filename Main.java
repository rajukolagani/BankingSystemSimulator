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
                        System.out.println("Exiting... Goodbye!");
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
        System.out.println("1. Create an Account");
        System.out.println("2. Perform Operations on Existing Accounts");
        System.out.println("3. Exit");
        System.out.print("Enter your choice: ");
    }
    private static void createAccountFlow() {
        try {
            System.out.print("Enter Full Name: ");
            String name = scanner.nextLine().trim();

            System.out.println("Select Account Type:");
            System.out.println("1. Savings Account");
            System.out.println("2. Current Account");
            System.out.print("Choice: ");
            int typeChoice = readInt();

            String accNum = bank.generateAccountNumber();
            Account account;

            if (typeChoice == 1) {
                account = new SavingsAccount(accNum, name, 0.0);
            } else if (typeChoice == 2) {
                account = new CurrentAccount(accNum, name, 0.0);
            } else {
                throw new InvalidChoiceException("Invalid account type selected.");
            }

            bank.addAccount(account);
            System.out.println("\n✅ Account Created Successfully!");
            System.out.println("Account Number: " + accNum);
            System.out.println("Holder Name: " + name);
            System.out.println("Account Type: " + (typeChoice == 1 ? "Savings" : "Current"));
        } catch (InvalidChoiceException e) {
            System.out.println(e.getMessage());
        }
    }
    private static void performOperationsFlow() {
        System.out.print("Enter account number (or type 'list' to view/search): ");
        String input = scanner.nextLine().trim();

        try {
            if (input.equalsIgnoreCase("list")) {
                System.out.print("Enter partial name to search (blank for all): ");
                String query = scanner.nextLine();
                List<Account> results = bank.searchByName(query);

                if (results.isEmpty()) System.out.println("No accounts found.");
                else results.forEach(a -> System.out.println(a.getAccountNumber() + " | " + a.getHolderName() + " | Balance: " + a.getBalance()));
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
                    case 5 -> runConcurrentDemo(account);
                    case 6 -> back = true;
                    default -> System.out.println("Invalid option. Try again.");
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
        System.out.println("4. Show Balance");
        System.out.println("5. Run Concurrent Demo (Multithreading)");
        System.out.println("6. Return to Main Menu");
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
            a.withdraw(amt);
            System.out.println("Withdrawal successful. New balance: " + a.getBalance());
        } catch (InvalidAmountException | InsufficientBalanceException e) {
            System.out.println("Error: " + e.getMessage());
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
        System.out.println("\nAccount Number: " + a.getAccountNumber());
        System.out.println("Holder: " + a.getHolderName());
        System.out.println("Balance: " + a.getBalance());
    }
    private static void runConcurrentDemo(Account a) {
        System.out.println("\n--- Starting Concurrent Transaction Demo ---");
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
        System.out.println("Demo finished. Final balance: " + a.getBalance());
    }
    private static int readInt() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Invalid number. Enter again: ");
            }
        }
    }

    private static double readDouble() {
        while (true) {
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Invalid number. Enter again: ");
            }
        }
    }
}
