package com.sbtms.service;

import com.sbtms.db.AccountDAOImpl;
import com.sbtms.interfaces.AccountDAO;
import com.sbtms.model.*;

import java.util.*;

/**
 * Analytics and Portfolio Risk Management Engine for SB-TMS.
 * Aggregates multi-account balances, calculates liquidity metrics, and visualizes financial health.
 * 
 * @author Anish Vyapari (Roll No: 25CA1012 | PRN: DY25ENGU0AIM012 | Batch: A/A1)
 * @institution Ramrao Adik Institute of Technology, Nerul
 */
public class AnalyticsService {
    private final AccountDAO accountDAO;

    public AnalyticsService() {
        this.accountDAO = new AccountDAOImpl();
    }

    public AnalyticsService(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    public Map<String, Object> getPortfolioMetrics() {
        List<Account> accounts = accountDAO.findAll();
        List<Transaction> transactions = accountDAO.getAllTransactions();

        double totalDeposits = 0.0;
        double savingsTotal = 0.0;
        double currentTotal = 0.0;
        double fdTotal = 0.0;
        double activeOverdraftTotal = 0.0;
        double approvedOverdraftLimitTotal = 0.0;

        int savingsCount = 0;
        int currentCount = 0;
        int fdCount = 0;

        for (Account acc : accounts) {
            totalDeposits += acc.getBalance();
            if (acc instanceof SavingsAccount) {
                savingsTotal += acc.getBalance();
                savingsCount++;
            } else if (acc instanceof CurrentAccount) {
                CurrentAccount ca = (CurrentAccount) acc;
                currentTotal += ca.getBalance();
                activeOverdraftTotal += ca.getUsedOverdraft();
                approvedOverdraftLimitTotal += ca.getOverdraftLimit();
                currentCount++;
            } else if (acc instanceof FixedDepositAccount) {
                fdTotal += acc.getBalance();
                fdCount++;
            }
        }

        double netAssets = totalDeposits - activeOverdraftTotal;
        double liquidityRatio = approvedOverdraftLimitTotal > 0 
                ? ((totalDeposits) / (totalDeposits + approvedOverdraftLimitTotal)) * 100.0 
                : 100.0;

        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("totalPortfolioValue", totalDeposits);
        metrics.put("netAssetValue", netAssets);
        metrics.put("totalSavingsDeposits", savingsTotal);
        metrics.put("totalCurrentDeposits", currentTotal);
        metrics.put("totalFixedDeposits", fdTotal);
        metrics.put("activeOverdraftLiability", activeOverdraftTotal);
        metrics.put("approvedOverdraftCapacity", approvedOverdraftLimitTotal);
        metrics.put("totalAccounts", accounts.size());
        metrics.put("savingsAccountCount", savingsCount);
        metrics.put("currentAccountCount", currentCount);
        metrics.put("fdAccountCount", fdCount);
        metrics.put("totalTransactionCount", transactions.size());
        metrics.put("liquidityRatio", Math.round(liquidityRatio * 100.0) / 100.0);

        // Account list summaries
        List<Map<String, Object>> accountSummaries = new ArrayList<>();
        for (Account acc : accounts) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("accountNumber", acc.getAccountNumber());
            item.put("holderName", acc.getHolderName());
            item.put("email", acc.getEmail());
            item.put("phone", acc.getPhone());
            item.put("accountType", acc.getAccountType());
            item.put("balance", acc.getBalance());
            item.put("availableFunds", acc.getAvailableFunds());
            item.put("status", acc.getStatus());

            if (acc instanceof SavingsAccount) {
                item.put("interestRate", ((SavingsAccount) acc).getAnnualInterestRate());
                item.put("minBalance", SavingsAccount.MINIMUM_OPERATING_BALANCE);
            } else if (acc instanceof CurrentAccount) {
                CurrentAccount ca = (CurrentAccount) acc;
                item.put("overdraftLimit", ca.getOverdraftLimit());
                item.put("usedOverdraft", ca.getUsedOverdraft());
            } else if (acc instanceof FixedDepositAccount) {
                FixedDepositAccount fda = (FixedDepositAccount) acc;
                item.put("interestRate", fda.getFixedInterestRate());
                item.put("tenureMonths", fda.getTenureMonths());
                item.put("isLiquidated", fda.isLiquidated());
            }
            accountSummaries.add(item);
        }
        metrics.put("accounts", accountSummaries);

        // Recent 10 transactions
        List<Transaction> recent = transactions.size() > 10 ? transactions.subList(0, 10) : transactions;
        metrics.put("recentTransactions", recent);

        return metrics;
    }
}
