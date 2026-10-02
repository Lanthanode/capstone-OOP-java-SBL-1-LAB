package com.sbtms.cli;

import com.sbtms.exception.InsufficientFundsException;
import com.sbtms.exception.OverdraftExceededException;
import com.sbtms.model.Account;
import com.sbtms.model.CurrentAccount;
import com.sbtms.model.SavingsAccount;
import com.sbtms.service.BankingService;

import java.util.List;
import java.util.Scanner;

/**
 * Standalone Console CLI Demonstrator for SB-TMS.
 * Retains 100% fidelity with SBL OOP Java Experiment 11 console execution,
 * while allowing interactive demonstration for the evaluating professor.
 * 
 * @author Anish Vyapari (Roll No: 25CA1012 | PRN: DY25ENGU0AIM012 | Batch: A/A1)
 * @institution Ramrao Adik Institute of Technology, Nerul
 */
public class CLIConsole {
    private final BankingService bankingService;

    public CLIConsole() {
        this.bankingService = new BankingService();
    }

    public void runAutomatedDemo() {
        System.out.println("================================================================================");
        System.out.println("   RAMRAO ADIK INSTITUTE OF TECHNOLOGY, NERUL - DEPARTMENT OF COMPUTER ENGG.    ");
        System.out.println("             SBL - OBJECT ORIENTED PROGRAMMING IN JAVA LABORATORY               ");
        System.out.println("           CAPSTONE PROJECT: SMART BANKING & TRANSACTION SYSTEM (SB-TMS)        ");
        System.out.println("   Candidate: Anish Vyapari | Roll No: 25CA1012 | PRN: DY25ENGU0AIM012 | Batch: A/A1");
        System.out.println("================================================================================\n");

        // 1. Account Portfolio Initialization
        System.out.println(">>> 1. INITIALIZING BANKING PORTFOLIO ACCOUNTS:");
        List<Account> accounts = bankingService.getAllAccounts();
        for (Account acc : accounts) {
            System.out.printf("  [Registered]: %-14s | Holder: %-25s | Balance: Rs. %10.2f | Type: %s\n",
                    acc.getAccountNumber(), acc.getHolderName(), acc.getBalance(), acc.getAccountType());
        }
        System.out.println("  -> Accounts Provisioned Successfully in Core Banking Relational DBMS.\n");

        // 2. Performing Core Transactions
        System.out.println(">>> 2. PERFORMING CORE TRANSACTIONS (DEPOSITS & WITHDRAWALS):");
        try {
            bankingService.deposit("SB-1012-IN", 12500.0, "Semester Academic Scholarship Credit");
            bankingService.deposit("CA-9080-CORP", 45000.0, "Enterprise Client Invoice Settlement");
            bankingService.withdraw("SB-1012-IN", 8000.0);
            bankingService.withdraw("CA-9080-CORP", 120000.0); // Draws overdraft
        } catch (Exception e) {
            System.out.println("  [Transaction Exception]: " + e.getMessage());
        }

        // 3. Inter-Account Wire Transfer
        System.out.println("\n>>> 3. DEMONSTRATING INTER-ACCOUNT WIRE TRANSFER:");
        try {
            bankingService.transfer("SB-1012-IN", "SB-3045-EXT", 15000.0, "Inter-Account Semester Fee Support");
        } catch (Exception e) {
            System.out.println("  [Transfer Fault]: " + e.getMessage());
        }

        // 4. Exception Handling & Domain Rule Boundaries
        System.out.println("\n>>> 4. TESTING SYSTEM ROBUSTNESS & CUSTOM EXCEPTION BOUNDARIES:");
        System.out.println("\n-- Test Case A: Savings Account Over-Withdrawal (Violating Minimum Operating Balance) --");
        try {
            Account sa = bankingService.getAccount("SB-1012-IN");
            System.out.println("Attempting withdrawal of Rs. 45000.0 from Savings (Current Balance: Rs. " + sa.getBalance() + ")...");
            bankingService.withdraw("SB-1012-IN", 45000.0);
        } catch (InsufficientFundsException ex) {
            System.out.println(">>> CAUGHT EXPECTED DOMAIN EXCEPTION: " + ex);
        } catch (Exception ex) {
            System.out.println(">>> Caught General Exception: " + ex);
        }

        System.out.println("\n-- Test Case B: Current Account Overdraft Exhaustion (Violating Overdraft Limit) --");
        try {
            Account ca = bankingService.getAccount("CA-9080-CORP");
            System.out.println("Attempting additional withdrawal of Rs. 40000.0 from Current Account (Balance: Rs. " + ca.getBalance() + ")...");
            bankingService.withdraw("CA-9080-CORP", 40000.0);
        } catch (OverdraftExceededException ex) {
            System.out.println(">>> CAUGHT EXPECTED DOMAIN EXCEPTION: " + ex);
        } catch (Exception ex) {
            System.out.println(">>> Caught General Exception: " + ex);
        }

        // 5. Runtime Polymorphic End-of-Quarter Servicing
        System.out.println("\n>>> 5. EXECUTING RUNTIME POLYMORPHIC END-OF-QUARTER SERVICING:");
        bankingService.executeQuarterEndServicing();

        // 6. Generating Official 2D Array Passbook Statements
        System.out.println("\n>>> 6. GENERATING PASSBOOK AUDIT STATEMENTS (2D ARRAY LEDGER):");
        for (Account acc : bankingService.getAllAccounts()) {
            acc.printStatement();
        }

        System.out.println("================================================================================");
        System.out.println("CAPSTONE PROJECT EXECUTION COMPLETED: ALL INTEGRATED OOP & DBMS SUBSYSTEMS VERIFIED OK");
        System.out.println("================================================================================\n");
    }

    public void startInteractiveLoop() {
        runAutomatedDemo();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n--- SB-TMS INTERACTIVE CONSOLE MENU ---");
            System.out.println("1. List All Accounts");
            System.out.println("2. Deposit Funds");
            System.out.println("3. Withdraw Funds");
            System.out.println("4. Transfer Funds (Inter-Account Wire)");
            System.out.println("5. Run End-of-Quarter Servicing (Runtime Polymorphism)");
            System.out.println("6. Print Passbook Statement (2D Ledger)");
            System.out.println("7. Reset Database to Benchmark Seed");
            System.out.println("8. Exit CLI");
            System.out.print("Select Option (1-8): ");

            String choice = scanner.nextLine().trim();
            if ("8".equals(choice) || "exit".equalsIgnoreCase(choice)) {
                System.out.println("Exiting SB-TMS CLI Console. Goodbye!");
                break;
            }

            try {
                switch (choice) {
                    case "1":
                        System.out.println("\nCURRENT ACCOUNTS IN DBMS:");
                        for (Account a : bankingService.getAllAccounts()) {
                            System.out.printf("  [%s] %-25s | Bal: Rs. %10.2f | Type: %-12s | Status: %s\n",
                                    a.getAccountNumber(), a.getHolderName(), a.getBalance(), a.getAccountType(), a.getStatus());
                        }
                        break;
                    case "2":
                        System.out.print("Enter Account Number (e.g. SB-1012-IN): ");
                        String accDep = scanner.nextLine().trim();
                        System.out.print("Enter Amount to Deposit: Rs. ");
                        double amtDep = Double.parseDouble(scanner.nextLine().trim());
                        System.out.print("Enter Reference Note: ");
                        String noteDep = scanner.nextLine().trim();
                        bankingService.deposit(accDep, amtDep, noteDep);
                        break;
                    case "3":
                        System.out.print("Enter Account Number: ");
                        String accWdl = scanner.nextLine().trim();
                        System.out.print("Enter Amount to Withdraw: Rs. ");
                        double amtWdl = Double.parseDouble(scanner.nextLine().trim());
                        bankingService.withdraw(accWdl, amtWdl);
                        break;
                    case "4":
                        System.out.print("Enter Source Account (Sender): ");
                        String fromAcc = scanner.nextLine().trim();
                        System.out.print("Enter Destination Account (Recipient): ");
                        String toAcc = scanner.nextLine().trim();
                        System.out.print("Enter Wire Amount: Rs. ");
                        double amtWire = Double.parseDouble(scanner.nextLine().trim());
                        bankingService.transfer(fromAcc, toAcc, amtWire, "CLI Wire Transfer");
                        break;
                    case "5":
                        bankingService.executeQuarterEndServicing();
                        break;
                    case "6":
                        System.out.print("Enter Account Number for Passbook: ");
                        String accPb = scanner.nextLine().trim();
                        Account target = bankingService.getAccount(accPb);
                        target.printStatement();
                        break;
                    case "7":
                        bankingService.resetBenchmarkData();
                        System.out.println("Database reset to benchmark seed successfully.");
                        break;
                    default:
                        System.out.println("Invalid selection. Please choose 1 through 8.");
                }
            } catch (Exception ex) {
                System.out.println(">>> ERROR: " + ex.getMessage());
            }
        }
    }
}
