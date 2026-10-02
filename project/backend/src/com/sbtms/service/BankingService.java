package com.sbtms.service;

import com.sbtms.db.AccountDAOImpl;
import com.sbtms.db.DatabaseManager;
import com.sbtms.exception.*;
import com.sbtms.interfaces.AccountDAO;
import com.sbtms.model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Enterprise Banking Service Orchestrator for SB-TMS.
 * Manages business validation, domain rules, ACID transactions, and polymorphic operations.
 * 
 * @author Anish Vyapari (Roll No: 25CA1012 | PRN: DY25ENGU0AIM012 | Batch: A/A1)
 * @institution Ramrao Adik Institute of Technology, Nerul
 */
public class BankingService {
    private final AccountDAO accountDAO;

    public BankingService() {
        this.accountDAO = new AccountDAOImpl();
    }

    public BankingService(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    public List<Account> getAllAccounts() {
        return accountDAO.findAll();
    }

    public Account getAccount(String accountNumber) throws AccountNotFoundException {
        Account acc = accountDAO.findByNumber(accountNumber);
        if (acc == null) {
            throw new AccountNotFoundException(accountNumber);
        }
        return acc;
    }

    public synchronized Account createAccount(Account account) throws BankingException {
        if (accountDAO.findByNumber(account.getAccountNumber()) != null) {
            throw new BankingException("ERR-BANK-005", "Account number already exists: " + account.getAccountNumber());
        }

        boolean saved = accountDAO.save(account);
        if (!saved) {
            throw new BankingException("ERR-BANK-006", "Failed to persist account to relational database.");
        }

        // Record opening transaction
        if (account.getBalance() > 0) {
            String txnId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            Transaction t = new Transaction(txnId, account.getAccountNumber(), "OPEN", account.getBalance(), 
                    account.getBalance(), "Initial Account Opening Deposit", null, null);
            accountDAO.recordTransaction(t);
        }

        accountDAO.recordAuditLog("ACCOUNT_CREATED", account.getAccountNumber(), 
                String.format("Created %s account for %s with initial deposit Rs. %.2f", 
                        account.getAccountType(), account.getHolderName(), account.getBalance()), 
                "SUCCESS", "127.0.0.1");

        return account;
    }

    public synchronized Transaction deposit(String accountNumber, double amount, String memo) 
            throws AccountNotFoundException, InvalidTransactionException {
        if (amount <= 0) {
            throw new InvalidTransactionException("Deposit amount must be positive. Received: Rs. " + amount);
        }

        Account acc = getAccount(accountNumber);
        String note = (memo != null && !memo.trim().isEmpty()) ? memo : "Counter Cash Deposit";
        
        acc.deposit(amount, note);
        accountDAO.update(acc);

        String txnId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Transaction txn = new Transaction(txnId, accountNumber, "DEP", amount, acc.getBalance(), note, null, null);
        accountDAO.recordTransaction(txn);

        accountDAO.recordAuditLog("DEPOSIT", accountNumber, 
                String.format("Deposited Rs. %.2f into %s. New Balance: Rs. %.2f", amount, accountNumber, acc.getBalance()), 
                "SUCCESS", "127.0.0.1");

        return txn;
    }

    public synchronized Transaction withdraw(String accountNumber, double amount) 
            throws AccountNotFoundException, InsufficientFundsException, OverdraftExceededException, BankingException {
        if (amount <= 0) {
            throw new InvalidTransactionException("Withdrawal amount must be positive. Received: Rs. " + amount);
        }

        Account acc = getAccount(accountNumber);
        double preBalance = acc.getBalance();

        try {
            acc.withdraw(amount);
        } catch (InsufficientFundsException | OverdraftExceededException e) {
            accountDAO.recordAuditLog("WITHDRAW_FAILED", accountNumber, 
                    "Withdrawal of Rs. " + amount + " rejected: " + e.getMessage(), "BLOCKED", "127.0.0.1");
            throw e;
        }

        accountDAO.update(acc);

        String txnType = "WDL";
        String note = "Counter Cash Withdrawal";
        if (acc instanceof CurrentAccount && ((CurrentAccount) acc).getUsedOverdraft() > 0) {
            txnType = "WDL-OD";
            note = String.format("Overdraft Withdrawal (Active OD: Rs. %.2f)", ((CurrentAccount) acc).getUsedOverdraft());
        }

        String txnId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Transaction txn = new Transaction(txnId, accountNumber, txnType, amount, acc.getBalance(), note, null, null);
        accountDAO.recordTransaction(txn);

        accountDAO.recordAuditLog("WITHDRAW_SUCCESS", accountNumber, 
                String.format("Withdrew Rs. %.2f from %s. Post-Balance: Rs. %.2f", amount, accountNumber, acc.getBalance()), 
                "SUCCESS", "127.0.0.1");

        return txn;
    }

    /**
     * Executes atomic inter-account funds transfer with ACID compliance.
     * Demonstrates dynamic method dispatch and strict rollback on failure.
     */
    public synchronized Map<String, Object> transfer(String sourceAccNo, String destAccNo, double amount, String memo) 
            throws BankingException {
        if (amount <= 0) {
            throw new InvalidTransactionException("Transfer amount must be positive. Received: Rs. " + amount);
        }
        if (sourceAccNo.equalsIgnoreCase(destAccNo)) {
            throw new InvalidTransactionException("Source and destination accounts must be different.");
        }

        Account sender = getAccount(sourceAccNo);
        Account recipient = getAccount(destAccNo);

        if (!"ACTIVE".equalsIgnoreCase(sender.getStatus())) {
            throw new InvalidTransactionException("Sender account " + sourceAccNo + " is inactive or frozen.");
        }
        if (!"ACTIVE".equalsIgnoreCase(recipient.getStatus())) {
            throw new InvalidTransactionException("Recipient account " + destAccNo + " is inactive or frozen.");
        }

        // ACID Transaction using JDBC
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // Deduct from sender (Polymorphic withdraw throws InsufficientFundsException / OverdraftExceededException)
                sender.withdraw(amount);

                // Credit recipient
                recipient.deposit(amount, "Wire Inflow from " + sender.getAccountNumber() + " (" + sender.getHolderName() + ")");

                // Update accounts in DB
                updateAccountWithConnection(conn, sender);
                updateAccountWithConnection(conn, recipient);

                // Insert transactions
                String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                String txnIdOut = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                String txnIdIn = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                String outNote = (memo != null && !memo.trim().isEmpty()) ? memo : "Wire Transfer to " + recipient.getAccountNumber();
                insertTransactionWithConnection(conn, txnIdOut, sender.getAccountNumber(), "TRANSFER-OUT", amount, 
                        sender.getBalance(), outNote, recipient.getAccountNumber(), now);

                insertTransactionWithConnection(conn, txnIdIn, recipient.getAccountNumber(), "TRANSFER-IN", amount, 
                        recipient.getBalance(), "Wire from " + sender.getAccountNumber(), sender.getAccountNumber(), now);

                conn.commit();

                accountDAO.recordAuditLog("TRANSFER_SUCCESS", sourceAccNo, 
                        String.format("Wire Transfer of Rs. %.2f to %s completed.", amount, destAccNo), 
                        "SUCCESS", "127.0.0.1");

                Map<String, Object> resp = new LinkedHashMap<>();
                resp.put("success", true);
                resp.put("amount", amount);
                resp.put("sourceAccount", sourceAccNo);
                resp.put("destAccount", destAccNo);
                resp.put("sourceBalance", sender.getBalance());
                resp.put("destBalance", recipient.getBalance());
                resp.put("timestamp", now);
                return resp;

            } catch (Exception ex) {
                conn.rollback();
                accountDAO.recordAuditLog("TRANSFER_FAILED", sourceAccNo, 
                        "Transfer of Rs. " + amount + " to " + destAccNo + " rolled back: " + ex.getMessage(), 
                        "FAILED", "127.0.0.1");
                if (ex instanceof BankingException) {
                    throw (BankingException) ex;
                }
                throw new BankingException("ERR-BANK-TX", "Wire transfer transaction rolled back: " + ex.getMessage(), ex);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new BankingException("ERR-BANK-DB", "Database error during wire transfer: " + e.getMessage(), e);
        }
    }

    /**
     * Executes Runtime Polymorphic End-of-Quarter Servicing across all accounts.
     * Directly reflects Experiment 11 Step 5 dynamic dispatch logic.
     */
    public synchronized List<Map<String, Object>> executeQuarterEndServicing() {
        List<Account> portfolio = accountDAO.findAll();
        List<Map<String, Object>> results = new ArrayList<>();

        System.out.println("\n>>> EXECUTING RUNTIME POLYMORPHIC END-OF-QUARTER SERVICING (DYNAMIC DISPATCH):");

        for (Account acc : portfolio) {
            double preBalance = acc.getBalance();
            double preOd = (acc instanceof CurrentAccount) ? ((CurrentAccount) acc).getUsedOverdraft() : 0.0;

            System.out.println("Processing Quarter-End for: " + acc.getAccountNumber() + " (" + acc.getClass().getSimpleName() + ")");
            
            // Dynamic Method Dispatch: Calls overridden applyPeriodicInterest() in SavingsAccount / CurrentAccount / FixedDepositAccount
            acc.applyPeriodicInterest();
            String auditMetric = acc.generateAuditSummary();

            // Persist updated account state to relational DBMS
            accountDAO.update(acc);

            double diff = acc.getBalance() - preBalance;
            double postOd = (acc instanceof CurrentAccount) ? ((CurrentAccount) acc).getUsedOverdraft() : 0.0;
            double odDiff = postOd - preOd;

            String eventType = "NONE";
            double eventAmount = 0.0;

            if (acc instanceof SavingsAccount && diff > 0) {
                eventType = "INTEREST_CREDITED";
                eventAmount = diff;
                String txnId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                accountDAO.recordTransaction(new Transaction(txnId, acc.getAccountNumber(), "INT", diff, acc.getBalance(), 
                        "Quarterly Savings Interest Credit", null, null));
            } else if (acc instanceof CurrentAccount && odDiff > 0) {
                eventType = "OVERDRAFT_FEE_LEVIED";
                eventAmount = odDiff;
                String txnId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                accountDAO.recordTransaction(new Transaction(txnId, acc.getAccountNumber(), "OD-LEVY", odDiff, acc.getBalance(), 
                        "3% Quarterly Overdraft Utilization Levy", null, null));
            } else if (acc instanceof FixedDepositAccount && diff > 0) {
                eventType = "COMPOUND_INTEREST_CREDITED";
                eventAmount = diff;
                String txnId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                accountDAO.recordTransaction(new Transaction(txnId, acc.getAccountNumber(), "INT-COMPOUND", diff, acc.getBalance(), 
                        "Quarterly Term Compounding Yield", null, null));
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("accountNumber", acc.getAccountNumber());
            item.put("holderName", acc.getHolderName());
            item.put("accountType", acc.getAccountType());
            item.put("preBalance", preBalance);
            item.put("postBalance", acc.getBalance());
            item.put("eventType", eventType);
            item.put("eventAmount", eventAmount);
            item.put("auditMetric", auditMetric);
            results.add(item);
        }

        accountDAO.recordAuditLog("QUARTER_END_BATCH", null, 
                "Successfully executed End-of-Quarter Servicing across " + portfolio.size() + " accounts via Runtime Polymorphic Dynamic Dispatch.", 
                "SUCCESS", "127.0.0.1");

        return results;
    }

    public Map<String, Object> getPassbookStatement(String accountNumber) throws AccountNotFoundException {
        Account acc = getAccount(accountNumber);
        List<Transaction> txns = accountDAO.getTransactionsForAccount(accountNumber);

        Map<String, Object> passbook = new LinkedHashMap<>();
        passbook.put("accountNumber", acc.getAccountNumber());
        passbook.put("holderName", acc.getHolderName());
        passbook.put("accountType", acc.getAccountType());
        passbook.put("balance", acc.getBalance());
        passbook.put("availableFunds", acc.getAvailableFunds());
        passbook.put("status", acc.getStatus());
        passbook.put("createdAt", acc.getCreatedAt());

        // 2D Array format matching Experiment 11 (TXN ID, TYPE, AMOUNT, BALANCE)
        List<Map<String, Object>> ledgerRows = new ArrayList<>();
        for (Transaction t : txns) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("txnId", t.getTxnId());
            row.put("type", t.getTxnType());
            row.put("amount", t.getAmount());
            row.put("balanceAfter", t.getBalanceAfter());
            row.put("referenceNote", t.getReferenceNote());
            row.put("timestamp", t.getTimestamp());
            row.put("counterparty", t.getCounterpartyAccount());
            ledgerRows.add(row);
        }
        passbook.put("ledger", ledgerRows);
        passbook.put("totalTransactions", ledgerRows.size());
        return passbook;
    }

    public List<AuditRecord> getAuditLogs(int limit) {
        return accountDAO.getAuditLogs(limit);
    }

    public List<Beneficiary> getBeneficiaries(String sourceAccount) {
        return accountDAO.getBeneficiariesForAccount(sourceAccount);
    }

    public boolean addBeneficiary(Beneficiary b) {
        return accountDAO.addBeneficiary(b);
    }

    public void resetBenchmarkData() {
        accountDAO.resetToBenchmarkSeed();
    }

    // --- JDBC TRANSACTION HELPERS ---
    private void updateAccountWithConnection(Connection conn, Account account) throws SQLException {
        String sql = "UPDATE ACCOUNTS SET balance = ?, used_overdraft = ? WHERE account_number = ?;";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, account.getBalance());
            double usedOd = 0.0;
            if (account instanceof CurrentAccount) {
                usedOd = ((CurrentAccount) account).getUsedOverdraft();
            }
            ps.setDouble(2, usedOd);
            ps.setString(3, account.getAccountNumber());
            ps.executeUpdate();
        }
    }

    private void insertTransactionWithConnection(Connection conn, String txnId, String accNo, String type, 
                                                 double amount, double balAfter, String note, String counterparty, 
                                                 String timestamp) throws SQLException {
        String sql = "INSERT INTO TRANSACTIONS (txn_id, account_number, txn_type, amount, balance_after, reference_note, counterparty_account, timestamp) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txnId);
            ps.setString(2, accNo);
            ps.setString(3, type);
            ps.setDouble(4, amount);
            ps.setDouble(5, balAfter);
            ps.setString(6, note);
            ps.setString(7, counterparty);
            ps.setString(8, timestamp);
            ps.executeUpdate();
        }
    }
}
