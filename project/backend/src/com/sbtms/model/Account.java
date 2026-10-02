package com.sbtms.model;

import com.sbtms.exception.BankingException;
import com.sbtms.exception.InsufficientFundsException;
import com.sbtms.exception.InvalidTransactionException;
import com.sbtms.exception.OverdraftExceededException;
import com.sbtms.interfaces.Auditable;
import com.sbtms.interfaces.TransactionProcessor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract Base Class defining the fundamental banking account structure.
 * Implements TransactionProcessor and Auditable interfaces.
 * Preserves 100% conceptual and architectural continuity with SBL OOP Java Experiment 11.
 * 
 * @author Anish Vyapari (Roll No: 25CA1012 | PRN: DY25ENGU0AIM012)
 * @institution Ramrao Adik Institute of Technology, Nerul
 */
public abstract class Account implements TransactionProcessor, Auditable {
    protected String accountNumber;
    protected String holderName;
    protected String email;
    protected String phone;
    protected double balance;
    protected String status;
    protected String createdAt;

    // 2D Array Ledger preserving exact backward compatibility with Experiment 11:
    // [Row: Records, Col: [0: TXN ID, 1: Type, 2: Amount (Rs.), 3: Balance (Rs.)]]
    protected String[][] transactionLedger;
    protected int txCount;

    // Dynamic collection for full relational persistence
    protected List<Transaction> transactions;

    // --- CONSTRUCTOR OVERLOADING ---

    public Account(String accountNumber, String holderName) {
        this(accountNumber, holderName, 0.0);
    }

    public Account(String accountNumber, String holderName, double initialDeposit) {
        this(accountNumber, holderName, "holder@sbtms.bank", "+91 98000 00000", initialDeposit, "ACTIVE", null);
    }

    public Account(String accountNumber, String holderName, String email, String phone, 
                   double initialDeposit, String status, String createdAt) {
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.email = email;
        this.phone = phone;
        this.balance = initialDeposit;
        this.status = status != null ? status : "ACTIVE";
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        this.transactionLedger = new String[100][4];
        this.txCount = 0;
        this.transactions = new ArrayList<>();

        if (initialDeposit > 0) {
            recordTransaction("OPEN", initialDeposit, balance, "Initial Account Opening Balance", null);
        }
    }

    /**
     * Records a transaction into both the Experiment 11 2D array ledger and the dynamic transaction list.
     */
    public synchronized void recordTransaction(String type, double amount, double postBalance, String note, String counterparty) {
        String txnId = "TXN-" + String.format("%04d", (txCount + 1));
        
        // 1. Maintain 2D array representation (Experiment 11 requirement)
        if (txCount < transactionLedger.length) {
            transactionLedger[txCount][0] = txnId;
            transactionLedger[txCount][1] = type;
            transactionLedger[txCount][2] = String.format("%.2f", amount);
            transactionLedger[txCount][3] = String.format("%.2f", postBalance);
            txCount++;
        }

        // 2. Add to structured collection
        Transaction txn = new Transaction(txnId, accountNumber, type, amount, postBalance, note, counterparty, null);
        transactions.add(txn);
    }

    // --- METHOD OVERLOADING: DEPOSIT ---

    @Override
    public void deposit(double amount) {
        deposit(amount, "Standard Counter Deposit");
    }

    @Override
    public synchronized void deposit(double amount, String referenceNote) {
        if (amount <= 0) {
            System.out.println("  [Deposit Error]: Invalid deposit amount: Rs. " + amount);
            return;
        }
        balance += amount;
        recordTransaction("DEP", amount, balance, referenceNote, null);
        System.out.printf("  [Deposit Confirmed]: Rs. %.2f deposited into [%s] (%s) | Balance: Rs. %.2f\n",
                amount, accountNumber, referenceNote, balance);
    }

    // --- INTER-ACCOUNT WIRE TRANSFER (Polymorphic interactions) ---

    @Override
    public synchronized void transfer(Account recipient, double amount) 
            throws InsufficientFundsException, OverdraftExceededException, BankingException {
        if (recipient == null) {
            throw new InvalidTransactionException("Recipient account cannot be null.");
        }
        if (this.accountNumber.equalsIgnoreCase(recipient.getAccountNumber())) {
            throw new InvalidTransactionException("Self-transfer prohibited: Source and destination accounts are identical.");
        }
        if (amount <= 0) {
            throw new InvalidTransactionException("Transfer amount must be strictly positive.");
        }

        System.out.println("\n  [Initiating Inter-Account Wire Transfer]:");
        System.out.printf("  From: %s (%s) -> To: %s (%s) | Amount: Rs. %.2f\n",
                this.accountNumber, this.holderName, recipient.accountNumber, recipient.holderName, amount);

        // Deduct from sender (may trigger domain exceptions)
        this.withdraw(amount);
        
        // Record specific transfer-out tag
        this.recordTransaction("TRANSFER-OUT", amount, this.balance, "Wire to " + recipient.getAccountNumber() + " (" + recipient.getHolderName() + ")", recipient.getAccountNumber());
        
        // Credit recipient
        recipient.balance += amount;
        recipient.recordTransaction("TRANSFER-IN", amount, recipient.balance, "Wire Inflow from " + this.accountNumber + " (" + this.holderName + ")", this.accountNumber);

        System.out.println("  [Wire Transfer Completed Successfully!]");
    }

    // --- ABSTRACT METHODS FOR RUNTIME POLYMORPHISM ---

    @Override
    public abstract void withdraw(double amount) throws InsufficientFundsException, OverdraftExceededException, BankingException;

    public abstract void applyPeriodicInterest();

    public abstract String getAccountType();

    public abstract double getAvailableFunds();

    // --- AUDITABLE IMPLEMENTATION ---

    @Override
    public void printStatement() {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.printf("OFFICIAL PASSBOOK STATEMENT | Account: %s (%s) | Holder: %s\n",
                accountNumber, getAccountType(), holderName);
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("%-10s | %-12s | %-15s | %-15s\n", "TXN ID", "TYPE", "AMOUNT (Rs.)", "BALANCE (Rs.)");
        System.out.println("--------------------------------------------------------------------------------");
        for (int i = 0; i < txCount; i++) {
            System.out.printf("%-10s | %-12s | %15s | %15s\n",
                    transactionLedger[i][0],
                    transactionLedger[i][1],
                    transactionLedger[i][2],
                    transactionLedger[i][3]);
        }
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("Closing Net Balance: Rs. %.2f | Available Funds: Rs. %.2f\n", balance, getAvailableFunds());
        System.out.println("--------------------------------------------------------------------------------");
    }

    // --- GETTERS & SETTERS (Encapsulation) ---

    public String getAccountNumber() { return accountNumber; }
    public String getHolderName() { return holderName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public String[][] getTransactionLedger() { return transactionLedger; }
    public int getTxCount() { return txCount; }
    @Override
    public List<Transaction> getTransactionList() { return Collections.unmodifiableList(transactions); }
    public void setTransactionList(List<Transaction> list) { 
        this.transactions = new ArrayList<>(list); 
        this.txCount = 0;
        for (Transaction t : list) {
            if (txCount < transactionLedger.length) {
                transactionLedger[txCount][0] = t.getTxnId();
                transactionLedger[txCount][1] = t.getTxnType();
                transactionLedger[txCount][2] = String.format("%.2f", t.getAmount());
                transactionLedger[txCount][3] = String.format("%.2f", t.getBalanceAfter());
                txCount++;
            }
        }
    }
}
