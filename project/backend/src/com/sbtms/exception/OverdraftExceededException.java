package com.sbtms.exception;

/**
 * Custom checked exception raised when a Current Account commercial withdrawal exceeds approved overdraft limit.
 * Directly evolves from Experiment 11 (Rule ERR-BANK-002).
 * 
 * @author Anish Vyapari (Roll No: 25CA1012 | PRN: DY25ENGU0AIM012)
 */
public class OverdraftExceededException extends BankingException {
    private final double requestedOverdraft;
    private final double maxOverdraftLimit;

    public OverdraftExceededException(String message, double requestedOverdraft, double maxOverdraftLimit) {
        super("ERR-BANK-002", message);
        this.requestedOverdraft = requestedOverdraft;
        this.maxOverdraftLimit = maxOverdraftLimit;
    }

    public double getRequestedOverdraft() {
        return requestedOverdraft;
    }

    public double getMaxOverdraftLimit() {
        return maxOverdraftLimit;
    }

    @Override
    public String toString() {
        return "OverdraftExceededException [ERR-BANK-002]: " + getMessage() + 
               " (Overdraft Requested: Rs. " + String.format("%.2f", requestedOverdraft) + 
               ", Approved Limit: Rs. " + String.format("%.2f", maxOverdraftLimit) + ")";
    }
}
