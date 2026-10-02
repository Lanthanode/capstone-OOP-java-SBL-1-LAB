package com.sbtms.model;

/**
 * Immutable audit log record for security, non-repudiation, and regulatory compliance.
 * 
 * @author Anish Vyapari (Roll No: 25CA1012 | PRN: DY25ENGU0AIM012)
 */
public class AuditRecord {
    private final int logId;
    private final String action;
    private final String accountNumber;
    private final String details;
    private final String status;
    private final String ipAddress;
    private final String timestamp;

    public AuditRecord(int logId, String action, String accountNumber, String details, 
                       String status, String ipAddress, String timestamp) {
        this.logId = logId;
        this.action = action;
        this.accountNumber = accountNumber;
        this.details = details;
        this.status = status;
        this.ipAddress = ipAddress;
        this.timestamp = timestamp;
    }

    public int getLogId() { return logId; }
    public String getAction() { return action; }
    public String getAccountNumber() { return accountNumber; }
    public String getDetails() { return details; }
    public String getStatus() { return status; }
    public String getIpAddress() { return ipAddress; }
    public String getTimestamp() { return timestamp; }
}
