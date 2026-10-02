package com.sbtms.exception;

/**
 * Base checked exception for domain-level banking errors in SB-TMS.
 * Part of SBL OOP Java Capstone Project.
 * 
 * @author Anish Vyapari (Roll No: 25CA1012 | PRN: DY25ENGU0AIM012)
 * @institution Ramrao Adik Institute of Technology, Nerul
 */
public class BankingException extends Exception {
    private final String errorCode;

    public BankingException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BankingException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + errorCode + "]: " + getMessage();
    }
}
