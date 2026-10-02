package com.sbtms.model;

import com.sbtms.exception.InvalidTransactionException;
import com.sbtms.exception.OverdraftExceededException;

/**
 * Concrete Subclass representing a Commercial Current Account with Overdraft credit facility.
 * Demonstrates Inheritance, Polymorphic dispatch, and Custom Exception throwing.
 * Directly evolves from Experiment 11.
 * 
 * @author Anish Vyapari (Roll No: 25CA1012 | PRN: DY25ENGU0AIM012)
 */
public class CurrentAccount extends Account {
    private double overdraftLimit;
    private double usedOverdraft;

    public CurrentAccount(String accountNumber, String holderName, double initialDeposit, double overdraftLimit) {
        super(accountNumber, holderName, initialDeposit);
        this.overdraftLimit = overdraftLimit;
        this.usedOverdraft = 0.0;
    }

    public CurrentAccount(String accountNumber, String holderName, String email, String phone, 
                          double initialDeposit, double overdraftLimit, double usedOverdraft, 
                          String status, String createdAt) {
        super(accountNumber, holderName, email, phone, initialDeposit, status, createdAt);
        this.overdraftLimit = overdraftLimit;
        this.usedOverdraft = usedOverdraft;
    }

    @Override
    public synchronized void withdraw(double amount) throws OverdraftExceededException, InvalidTransactionException {
        if (amount <= 0) {
            throw new InvalidTransactionException("Withdrawal request must be positive. Attempted: Rs. " + amount);
        }

        if (amount <= balance) {
            // Standard withdrawal using liquid credit balance
            balance -= amount;
            recordTransaction("WDL", amount, balance, "Corporate Working Capital Dispense", null);
            System.out.printf("  [Current Withdrawal]: Rs. %.2f debited from [%s] | Balance: Rs. %.2f\n",
                    amount, accountNumber, balance);
        } else {
            // Deficit triggers commercial overdraft credit line
            double deficit = amount - balance;
            if ((usedOverdraft + deficit) > overdraftLimit) {
                throw new OverdraftExceededException(
                        "Overdraft Limit Exhausted! Deficit required: Rs. " + String.format("%.2f", deficit),
                        (usedOverdraft + deficit), overdraftLimit
                );
            }
            usedOverdraft += deficit;
            balance = 0.0;
            recordTransaction("WDL-OD", amount, balance, 
                    String.format("Overdraft Drawdown: Rs. %.2f (Total Active OD: Rs. %.2f / Rs. %.2f)", 
                            deficit, usedOverdraft, overdraftLimit), null);
            System.out.printf("  [Current Overdraft Withdrawal]: Rs. %.2f debited. Current Overdraft Utilized: Rs. %.2f / Rs. %.2f\n",
                    amount, usedOverdraft, overdraftLimit);
        }
    }

    @Override
    public synchronized void deposit(double amount, String referenceNote) {
        if (amount <= 0) {
            System.out.println("  [Deposit Error]: Invalid deposit amount: Rs. " + amount);
            return;
        }

        // If active overdraft exists, credit replenishes the overdraft first
        if (usedOverdraft > 0) {
            if (amount <= usedOverdraft) {
                usedOverdraft -= amount;
                recordTransaction("DEP-OD-REPAY", amount, balance, 
                        referenceNote + " (Overdraft Repaid: Rs. " + amount + " | Remaining OD: Rs. " + usedOverdraft + ")", null);
                System.out.printf("  [Deposit OD Repayment]: Rs. %.2f applied to overdraft. Remaining OD: Rs. %.2f | Balance: Rs. %.2f\n",
                        amount, usedOverdraft, balance);
                return;
            } else {
                double excess = amount - usedOverdraft;
                double odCleared = usedOverdraft;
                usedOverdraft = 0.0;
                balance += excess;
                recordTransaction("DEP", amount, balance, 
                        referenceNote + " (OD Cleared: Rs. " + odCleared + " | Credit Added: Rs. " + excess + ")", null);
                System.out.printf("  [Deposit Confirmed]: OD Cleared! Net balance: Rs. %.2f\n", balance);
                return;
            }
        }

        super.deposit(amount, referenceNote);
    }

    @Override
    public synchronized void applyPeriodicInterest() {
        if (usedOverdraft > 0) {
            double overdraftInterestFee = usedOverdraft * 0.03; // 3% fee on active credit line
            usedOverdraft += overdraftInterestFee;
            recordTransaction("OD-LEVY", overdraftInterestFee, balance, 
                    String.format("Quarterly Overdraft Utilization Fee (3%%). Outstanding OD: Rs. %.2f", usedOverdraft), null);
            System.out.printf("  [Overdraft Levy]: Added overdraft utilization fee of Rs. %.2f | Outstanding OD: Rs. %.2f\n",
                    overdraftInterestFee, usedOverdraft);
        } else {
            System.out.println("  [Current Account]: Commercial accounts earn zero interest. No active overdraft charges incurred.");
        }
    }

    @Override
    public String generateAuditSummary() {
        String msg = String.format("Current Account %s | Holder: %s | Active Overdraft: Rs. %.2f / Rs. %.2f | Net Balance: Rs. %.2f",
                accountNumber, holderName, usedOverdraft, overdraftLimit, balance);
        System.out.printf("  [Audit Metric]: %s\n", msg);
        return msg;
    }

    @Override
    public String getAccountType() {
        return "CURRENT";
    }

    @Override
    public double getAvailableFunds() {
        return balance + Math.max(0.0, overdraftLimit - usedOverdraft);
    }

    public double getOverdraftLimit() {
        return overdraftLimit;
    }

    public void setOverdraftLimit(double overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
    }

    public double getUsedOverdraft() {
        return usedOverdraft;
    }

    public void setUsedOverdraft(double usedOverdraft) {
        this.usedOverdraft = usedOverdraft;
    }
}
