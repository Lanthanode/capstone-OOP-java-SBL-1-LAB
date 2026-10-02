package com.sbtms.model;

import com.sbtms.exception.BankingException;
import com.sbtms.exception.InvalidTransactionException;

/**
 * Concrete Subclass representing a Term Fixed Deposit (FD) Investment Account.
 * Extends the OOP hierarchy introduced in Experiment 11 to demonstrate deep domain extensibility.
 * 
 * @author Anish Vyapari (Roll No: 25CA1012 | PRN: DY25ENGU0AIM012 | Batch: A/A1)
 */
public class FixedDepositAccount extends Account {
    private int tenureMonths;
    private double fixedInterestRate; // e.g. 7.50% p.a.
    private boolean liquidated;

    public FixedDepositAccount(String accountNumber, String holderName, double principalAmount, 
                               int tenureMonths, double interestRate) {
        super(accountNumber, holderName, principalAmount);
        this.tenureMonths = tenureMonths;
        this.fixedInterestRate = interestRate;
        this.liquidated = false;
    }

    public FixedDepositAccount(String accountNumber, String holderName, String email, String phone, 
                               double principalAmount, int tenureMonths, double interestRate, 
                               String status, String createdAt) {
        super(accountNumber, holderName, email, phone, principalAmount, status, createdAt);
        this.tenureMonths = tenureMonths;
        this.fixedInterestRate = interestRate;
        this.liquidated = "CLOSED".equalsIgnoreCase(status);
    }

    @Override
    public synchronized void withdraw(double amount) throws BankingException {
        if (liquidated) {
            throw new InvalidTransactionException("Fixed Deposit account " + accountNumber + " is already closed/liquidated.");
        }
        if (amount > balance) {
            throw new InvalidTransactionException("Premature liquidation cannot exceed principal balance of Rs. " + balance);
        }

        // Premature liquidation penalty rule
        double penalty = amount * 0.02; // 2% penalty for premature termination
        double netPayout = amount - penalty;
        balance = 0.0;
        liquidated = true;
        this.status = "CLOSED";

        recordTransaction("WDL-LIQUIDATE", amount, balance, 
                String.format("Premature Term Liquidation (Penalty: Rs. %.2f | Net Disbursed: Rs. %.2f)", penalty, netPayout), null);

        System.out.printf("  [FD Premature Liquidation]: Rs. %.2f closed from [%s]. Net Payout: Rs. %.2f (Penalty: Rs. %.2f)\n",
                amount, accountNumber, netPayout, penalty);
    }

    @Override
    public synchronized void applyPeriodicInterest() {
        if (!liquidated && balance > 0) {
            // Quarterly compound interest: A = P(1 + r/400)
            double quarterlyInterest = balance * (fixedInterestRate / 400.0);
            balance += quarterlyInterest;
            recordTransaction("INT-COMPOUND", quarterlyInterest, balance, 
                    String.format("Quarterly Compounding Accrual (@%.2f%% p.a., %d-Mo Term)", fixedInterestRate, tenureMonths), null);
            System.out.printf("  [FD Compounding Credit]: Added interest Rs. %.2f | Maturity Accrual: Rs. %.2f\n",
                    quarterlyInterest, balance);
        }
    }

    @Override
    public String generateAuditSummary() {
        String msg = String.format("Fixed Deposit %s | Holder: %s | Principal: Rs. %.2f | Rate: %.2f%% | Tenure: %d Months | Status: %s",
                accountNumber, holderName, balance, fixedInterestRate, tenureMonths, status);
        System.out.println("  [Audit Metric]: " + msg);
        return msg;
    }

    @Override
    public String getAccountType() {
        return "FIXED_DEPOSIT";
    }

    @Override
    public double getAvailableFunds() {
        return liquidated ? 0.0 : balance;
    }

    public int getTenureMonths() {
        return tenureMonths;
    }

    public void setTenureMonths(int tenureMonths) {
        this.tenureMonths = tenureMonths;
    }

    public double getFixedInterestRate() {
        return fixedInterestRate;
    }

    public void setFixedInterestRate(double fixedInterestRate) {
        this.fixedInterestRate = fixedInterestRate;
    }

    public boolean isLiquidated() {
        return liquidated;
    }
}
