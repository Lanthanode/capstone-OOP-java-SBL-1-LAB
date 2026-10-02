package com.sbtms.exception;

/**
 * Custom checked exception raised when transaction parameters are invalid (e.g. negative amount, self-transfer, inactive account).
 * 
 * @author Anish Vyapari (Roll No: 25CA1012 | PRN: DY25ENGU0AIM012)
 */
public class InvalidTransactionException extends BankingException {
    public InvalidTransactionException(String message) {
        super("ERR-BANK-004", message);
    }

    public InvalidTransactionException(String message, Throwable cause) {
        super("ERR-BANK-004", message, cause);
    }
}
