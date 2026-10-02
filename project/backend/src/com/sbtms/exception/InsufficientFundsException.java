package com.sbtms.exception;

/**
 * Custom checked exception raised when an account withdrawal violates minimum operating balance rules.
 * Directly evolves from Experiment 11 (Rule ERR-BANK-001).
 * 
 * @author Anish Vyapari (Roll No: 25CA1012 | PRN: DY25ENGU0AIM012)
 */
public class InsufficientFundsException extends BankingException {
    private final double availableBalance;
    private final double attemptedAmount;

    public InsufficientFundsException(String message, double availableBalance, double attemptedAmount) {
        super("ERR-BANK-001", message);
        this.availableBalance = availableBalance;
        this.attemptedAmount = attemptedAmount;
    }

    public double getAvailableBalance() {
        return availableBalance;
    }

    public double getAttemptedAmount() {
        return attemptedAmount;
    }

    @Override
    public String toString() {
        return "InsufficientFundsException [ERR-BANK-001]: " + getMessage() + 
               " (Available: Rs. " + String.format("%.2f", availableBalance) + 
               ", Attempted: Rs. " + String.format("%.2f", attemptedAmount) + ")";
    }
}
