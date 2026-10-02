package com.sbtms.model;

import com.sbtms.exception.InsufficientFundsException;
import com.sbtms.exception.InvalidTransactionException;

/**
 * Concrete Subclass representing a Retail Savings Account.
 * Demonstrates Single Inheritance, super keyword, and method overriding.
 * Directly evolves from Experiment 11.
 * 
 * @author Anish Vyapari (Roll No: 25CA1012 | PRN: DY25ENGU0AIM012)
 */
public class SavingsAccount extends Account {
    private double annualInterestRate; // e.g. 4.25% p.a.
    public static final double MINIMUM_OPERATING_BALANCE = 2000.0; // Statutory reserve rule

    public SavingsAccount(String accountNumber, String holderName, double initialDeposit, double interestRate) {
        super(accountNumber, holderName, initialDeposit);
        this.annualInterestRate = interestRate;
    }

    public SavingsAccount(String accountNumber, String holderName, String email, String phone, 
                          double initialDeposit, double interestRate, String status, String createdAt) {
        super(accountNumber, holderName, email, phone, initialDeposit, status, createdAt);
        this.annualInterestRate = interestRate;
    }

    @Override
    public synchronized void withdraw(double amount) throws InsufficientFundsException, InvalidTransactionException {
        if (amount <= 0) {
            throw new InvalidTransactionException("Withdrawal request must be positive. Attempted: Rs. " + amount);
        }
        if ((balance - amount) < MINIMUM_OPERATING_BALANCE) {
            throw new InsufficientFundsException(
                    "Savings Minimum Balance Rule Violated (Required Threshold: Rs. " + MINIMUM_OPERATING_BALANCE + ")",
                    balance, amount
            );
        }
        balance -= amount;
        recordTransaction("WDL", amount, balance, "Savings Counter Dispense", null);
        System.out.printf("  [Savings Withdrawal]: Rs. %.2f dispensed from [%s] | Balance: Rs. %.2f\n",
                amount, accountNumber, balance);
    }

    @Override
    public synchronized void applyPeriodicInterest() {
        double quarterlyInterest = (balance * (annualInterestRate / 100.0)) / 4.0;
        balance += quarterlyInterest;
        recordTransaction("INT", quarterlyInterest, balance, 
                String.format("Quarterly Interest Accrual (@%.2f%% p.a.)", annualInterestRate), null);
        System.out.printf("  [Interest Credit]: Added quarterly interest Rs. %.2f (@%.2f%% p.a.) | New Balance: Rs. %.2f\n",
                quarterlyInterest, annualInterestRate, balance);
    }

    @Override
    public String generateAuditSummary() {
        String msg = String.format("Savings Account %s | Holder: %s | Balance: Rs. %.2f | Reserve Status: %s",
                accountNumber, holderName, balance, 
                (balance >= MINIMUM_OPERATING_BALANCE ? "COMPLIANT (>= Rs. 2000)" : "NON-COMPLIANT"));
        System.out.println("  [Audit Metric]: " + msg);
        return msg;
    }

    @Override
    public String getAccountType() {
        return "SAVINGS";
    }

    @Override
    public double getAvailableFunds() {
        return Math.max(0.0, balance - MINIMUM_OPERATING_BALANCE);
    }

    public double getAnnualInterestRate() {
        return annualInterestRate;
    }

    public void setAnnualInterestRate(double annualInterestRate) {
        this.annualInterestRate = annualInterestRate;
    }

    public static double getMinimumOperatingBalance() {
        return MINIMUM_OPERATING_BALANCE;
    }
}
