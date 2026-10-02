package com.sbtms.interfaces;

import com.sbtms.model.Account;
import com.sbtms.model.Transaction;
import com.sbtms.model.Beneficiary;
import com.sbtms.model.AuditRecord;
import java.util.List;

/**
 * Data Access Object (DAO) interface establishing the DBMS & SQL contract.
 * Separates the Java domain layer from the relational persistence layer.
 * 
 * @author Anish Vyapari (Roll No: 25CA1012 | PRN: DY25ENGU0AIM012)
 */
public interface AccountDAO {
    List<Account> findAll();
    Account findByNumber(String accountNumber);
    boolean save(Account account);
    boolean update(Account account);
    boolean delete(String accountNumber);

    List<Transaction> getTransactionsForAccount(String accountNumber);
    List<Transaction> getAllTransactions();
    boolean recordTransaction(Transaction transaction);

    List<Beneficiary> getBeneficiariesForAccount(String sourceAccount);
    boolean addBeneficiary(Beneficiary beneficiary);

    List<AuditRecord> getAuditLogs(int limit);
    boolean recordAuditLog(String action, String accountNumber, String details, String status, String ipAddress);

    void resetToBenchmarkSeed();
}
