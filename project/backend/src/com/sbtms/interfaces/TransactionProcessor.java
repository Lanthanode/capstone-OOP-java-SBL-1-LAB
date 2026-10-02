package com.sbtms.interfaces;

import com.sbtms.exception.BankingException;
import com.sbtms.exception.InsufficientFundsException;
import com.sbtms.exception.OverdraftExceededException;
import com.sbtms.model.Account;

/**
 * Interface defining the behavioral contract for all transactional banking operations.
 * Demonstrates method overloading and polymorphic dispatch as specified in Experiment 11.
 * 
 * @author Anish Vyapari (Roll No: 25CA1012 | PRN: DY25ENGU0AIM012)
 */
public interface TransactionProcessor {
    /**
     * Standard deposit without memo.
     * @param amount positive deposit sum
     */
    void deposit(double amount);

    /**
     * Overloaded deposit with an explicit audit memo/reference note.
     * @param amount positive deposit sum
     * @param referenceNote descriptive transaction memo
     */
    void deposit(double amount, String referenceNote);

    /**
     * Dispenses funds from the account, enforcing account-specific domain boundary rules.
     * @param amount sum to withdraw
     * @throws InsufficientFundsException if minimum operating balance rule is breached
     * @throws OverdraftExceededException if overdraft capacity is exhausted
     * @throws BankingException on other domain violations
     */
    void withdraw(double amount) throws InsufficientFundsException, OverdraftExceededException, BankingException;

    /**
     * Executes atomic inter-account funds transfer from this account to a recipient account.
     * @param recipient target Account object
     * @param amount sum to transfer
     * @throws InsufficientFundsException if minimum operating balance rule is breached
     * @throws OverdraftExceededException if overdraft capacity is exhausted
     * @throws BankingException on other domain violations
     */
    void transfer(Account recipient, double amount) throws InsufficientFundsException, OverdraftExceededException, BankingException;
}
