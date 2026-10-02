package com.sbtms.interfaces;

import com.sbtms.model.Transaction;
import java.util.List;

/**
 * Interface defining audit and ledger inspection capabilities.
 * Implemented by all banking account types.
 * Directly evolves from Experiment 11.
 * 
 * @author Anish Vyapari (Roll No: 25CA1012 | PRN: DY25ENGU0AIM012)
 */
public interface Auditable {
    /**
     * Prints an official formatted 2D array passbook statement to standard output.
     */
    void printStatement();

    /**
     * Generates a single-line audit metric summary string for end-of-quarter inspection.
     * @return audit summary string
     */
    String generateAuditSummary();

    /**
     * Returns the structured transaction history for reporting and persistence.
     * @return list of transactions
     */
    List<Transaction> getTransactionList();
}
