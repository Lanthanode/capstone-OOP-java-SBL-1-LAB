package com.sbtms.db;

import com.sbtms.interfaces.AccountDAO;
import com.sbtms.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC Data Access Object (DAO) implementation for Relational DBMS Persistence.
 * Translates SQL relational tuples into rich Java OOP objects using Polymorphic Factory Mapping.
 * 
 * @author Anish Vyapari (Roll No: 25CA1012 | PRN: DY25ENGU0AIM012 | Batch: A/A1)
 * @institution Ramrao Adik Institute of Technology, Nerul
 */
public class AccountDAOImpl implements AccountDAO {

    @Override
    public List<Account> findAll() {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM ACCOUNTS ORDER BY created_at ASC;";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Account acc = mapRowToAccount(rs);
                if (acc != null) {
                    acc.setTransactionList(getTransactionsForAccount(acc.getAccountNumber()));
                    list.add(acc);
                }
            }
        } catch (SQLException e) {
            System.err.println("[AccountDAO Error]: findAll failed: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Account findByNumber(String accountNumber) {
        String sql = "SELECT * FROM ACCOUNTS WHERE account_number = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Account acc = mapRowToAccount(rs);
                    if (acc != null) {
                        acc.setTransactionList(getTransactionsForAccount(accountNumber));
                    }
                    return acc;
                }
            }
        } catch (SQLException e) {
            System.err.println("[AccountDAO Error]: findByNumber failed: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean save(Account account) {
        String sql = "INSERT INTO ACCOUNTS (account_number, holder_name, email, phone, account_type, " +
                     "balance, interest_rate, min_balance, overdraft_limit, used_overdraft, tenure_months, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, account.getAccountNumber());
            ps.setString(2, account.getHolderName());
            ps.setString(3, account.getEmail());
            ps.setString(4, account.getPhone());
            ps.setString(5, account.getAccountType());
            ps.setDouble(6, account.getBalance());

            if (account instanceof SavingsAccount) {
                SavingsAccount sa = (SavingsAccount) account;
                ps.setDouble(7, sa.getAnnualInterestRate());
                ps.setDouble(8, SavingsAccount.MINIMUM_OPERATING_BALANCE);
                ps.setDouble(9, 0.0);
                ps.setDouble(10, 0.0);
                ps.setInt(11, 0);
            } else if (account instanceof CurrentAccount) {
                CurrentAccount ca = (CurrentAccount) account;
                ps.setDouble(7, 0.0);
                ps.setDouble(8, 0.0);
                ps.setDouble(9, ca.getOverdraftLimit());
                ps.setDouble(10, ca.getUsedOverdraft());
                ps.setInt(11, 0);
            } else if (account instanceof FixedDepositAccount) {
                FixedDepositAccount fda = (FixedDepositAccount) account;
                ps.setDouble(7, fda.getFixedInterestRate());
                ps.setDouble(8, 0.0);
                ps.setDouble(9, 0.0);
                ps.setDouble(10, 0.0);
                ps.setInt(11, fda.getTenureMonths());
            } else {
                ps.setDouble(7, 0.0);
                ps.setDouble(8, 0.0);
                ps.setDouble(9, 0.0);
                ps.setDouble(10, 0.0);
                ps.setInt(11, 0);
            }

            ps.setString(12, account.getStatus());
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[AccountDAO Error]: save failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(Account account) {
        String sql = "UPDATE ACCOUNTS SET balance = ?, status = ?, used_overdraft = ? WHERE account_number = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, account.getBalance());
            ps.setString(2, account.getStatus());
            double usedOd = 0.0;
            if (account instanceof CurrentAccount) {
                usedOd = ((CurrentAccount) account).getUsedOverdraft();
            }
            ps.setDouble(3, usedOd);
            ps.setString(4, account.getAccountNumber());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AccountDAO Error]: update failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(String accountNumber) {
        String sql = "DELETE FROM ACCOUNTS WHERE account_number = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AccountDAO Error]: delete failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Transaction> getTransactionsForAccount(String accountNumber) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM TRANSACTIONS WHERE account_number = ? ORDER BY timestamp ASC, txn_id ASC;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Transaction(
                            rs.getString("txn_id"),
                            rs.getString("account_number"),
                            rs.getString("txn_type"),
                            rs.getDouble("amount"),
                            rs.getDouble("balance_after"),
                            rs.getString("reference_note"),
                            rs.getString("counterparty_account"),
                            rs.getString("timestamp")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[AccountDAO Error]: getTransactionsForAccount failed: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Transaction> getAllTransactions() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM TRANSACTIONS ORDER BY timestamp DESC LIMIT 200;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Transaction(
                        rs.getString("txn_id"),
                        rs.getString("account_number"),
                        rs.getString("txn_type"),
                        rs.getDouble("amount"),
                        rs.getDouble("balance_after"),
                        rs.getString("reference_note"),
                        rs.getString("counterparty_account"),
                        rs.getString("timestamp")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[AccountDAO Error]: getAllTransactions failed: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean recordTransaction(Transaction t) {
        String sql = "INSERT INTO TRANSACTIONS (txn_id, account_number, txn_type, amount, balance_after, reference_note, counterparty_account, timestamp) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, t.getTxnId());
            ps.setString(2, t.getAccountNumber());
            ps.setString(3, t.getTxnType());
            ps.setDouble(4, t.getAmount());
            ps.setDouble(5, t.getBalanceAfter());
            ps.setString(6, t.getReferenceNote());
            ps.setString(7, t.getCounterpartyAccount());
            ps.setString(8, t.getTimestamp());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AccountDAO Error]: recordTransaction failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Beneficiary> getBeneficiariesForAccount(String sourceAccount) {
        List<Beneficiary> list = new ArrayList<>();
        String sql = "SELECT * FROM BENEFICIARIES WHERE source_account = ? ORDER BY added_at DESC;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, sourceAccount);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Beneficiary(
                            rs.getInt("id"),
                            rs.getString("source_account"),
                            rs.getString("beneficiary_account"),
                            rs.getString("beneficiary_name"),
                            rs.getString("bank_ifsc"),
                            rs.getDouble("max_limit"),
                            rs.getString("added_at")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[AccountDAO Error]: getBeneficiariesForAccount failed: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean addBeneficiary(Beneficiary b) {
        String sql = "INSERT INTO BENEFICIARIES (source_account, beneficiary_account, beneficiary_name, bank_ifsc, max_limit) " +
                     "VALUES (?, ?, ?, ?, ?);";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, b.getSourceAccount());
            ps.setString(2, b.getBeneficiaryAccount());
            ps.setString(3, b.getBeneficiaryName());
            ps.setString(4, b.getBankIfsc());
            ps.setDouble(5, b.getMaxLimit());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AccountDAO Error]: addBeneficiary failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<AuditRecord> getAuditLogs(int limit) {
        List<AuditRecord> list = new ArrayList<>();
        String sql = "SELECT * FROM AUDIT_LOGS ORDER BY timestamp DESC LIMIT ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new AuditRecord(
                            rs.getInt("log_id"),
                            rs.getString("action"),
                            rs.getString("account_number"),
                            rs.getString("details"),
                            rs.getString("status"),
                            rs.getString("ip_address"),
                            rs.getString("timestamp")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[AccountDAO Error]: getAuditLogs failed: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean recordAuditLog(String action, String accountNumber, String details, String status, String ipAddress) {
        String sql = "INSERT INTO AUDIT_LOGS (action, account_number, details, status, ip_address) VALUES (?, ?, ?, ?, ?);";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, action);
            ps.setString(2, accountNumber);
            ps.setString(3, details);
            ps.setString(4, status);
            ps.setString(5, ipAddress != null ? ipAddress : "127.0.0.1");

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AccountDAO Error]: recordAuditLog failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void resetToBenchmarkSeed() {
        try (Connection conn = DatabaseManager.getConnection()) {
            DatabaseManager.executeSqlScript(conn, "database/seed.sql");
            System.out.println("[AccountDAO]: Database successfully restored to Experiment 11 Benchmark Seed.");
        } catch (Exception e) {
            System.err.println("[AccountDAO Error]: resetToBenchmarkSeed failed: " + e.getMessage());
        }
    }

    // --- POLYMORPHIC FACTORY ROW MAPPER ---
    private Account mapRowToAccount(ResultSet rs) throws SQLException {
        String type = rs.getString("account_type");
        String accNo = rs.getString("account_number");
        String name = rs.getString("holder_name");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        double balance = rs.getDouble("balance");
        String status = rs.getString("status");
        String createdAt = rs.getString("created_at");

        if ("SAVINGS".equalsIgnoreCase(type)) {
            double rate = rs.getDouble("interest_rate");
            return new SavingsAccount(accNo, name, email, phone, balance, rate, status, createdAt);
        } else if ("CURRENT".equalsIgnoreCase(type)) {
            double limit = rs.getDouble("overdraft_limit");
            double usedOd = rs.getDouble("used_overdraft");
            return new CurrentAccount(accNo, name, email, phone, balance, limit, usedOd, status, createdAt);
        } else if ("FIXED_DEPOSIT".equalsIgnoreCase(type)) {
            int tenure = rs.getInt("tenure_months");
            double rate = rs.getDouble("interest_rate");
            return new FixedDepositAccount(accNo, name, email, phone, balance, tenure, rate, status, createdAt);
        }
        return null;
    }
}
