package com.sbtms.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Immutable entity representing a banking financial transaction.
 * Bridges the in-memory 2D array representation and the relational SQL DBMS table.
 * 
 * @author Anish Vyapari (Roll No: 25CA1012 | PRN: DY25ENGU0AIM012)
 */
public class Transaction {
    private final String txnId;
    private final String accountNumber;
    private final String txnType; // OPEN, DEP, WDL, WDL-OD, TRANSFER-OUT, TRANSFER-IN, INT, OD-LEVY
    private final double amount;
    private final double balanceAfter;
    private final String referenceNote;
    private final String counterpartyAccount;
    private final String timestamp;

    public Transaction(String txnId, String accountNumber, String txnType, double amount, 
                       double balanceAfter, String referenceNote, String counterpartyAccount, String timestamp) {
        this.txnId = txnId;
        this.accountNumber = accountNumber;
        this.txnType = txnType;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.referenceNote = referenceNote != null ? referenceNote : "";
        this.counterpartyAccount = counterpartyAccount;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public Transaction(String txnId, String accountNumber, String txnType, double amount, double balanceAfter, String referenceNote) {
        this(txnId, accountNumber, txnType, amount, balanceAfter, referenceNote, null, null);
    }

    public String getTxnId() { return txnId; }
    public String getAccountNumber() { return accountNumber; }
    public String getTxnType() { return txnType; }
    public double getAmount() { return amount; }
    public double getBalanceAfter() { return balanceAfter; }
    public String getReferenceNote() { return referenceNote; }
    public String getCounterpartyAccount() { return counterpartyAccount; }
    public String getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("[%s] %-12s | Rs. %10.2f | Balance: Rs. %10.2f | %s", 
                txnId, txnType, amount, balanceAfter, referenceNote);
    }
}
