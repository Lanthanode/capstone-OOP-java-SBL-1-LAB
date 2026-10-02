-- ==============================================================================
-- SMART BANKING TRANSACTION & ACCOUNT PORTFOLIO MANAGEMENT SYSTEM (SB-TMS)
-- SEED DATA SCRIPT - INITIALIZING EXPERIMENT 11 BENCHMARK DATASET
-- Candidate: Anish Vyapari | Roll No: 25CA1012 | PRN: DY25ENGU0AIM012
-- ==============================================================================

-- Clean up existing data for fresh seed
DELETE FROM TRANSACTIONS;
DELETE FROM BENEFICIARIES;
DELETE FROM AUDIT_LOGS;
DELETE FROM ACCOUNTS;
DELETE FROM SYSTEM_METRICS;

-- 1. Insert Accounts (Aligning directly with Experiment 11 + Fixed Deposit extension)
INSERT INTO ACCOUNTS (account_number, holder_name, email, phone, account_type, balance, interest_rate, min_balance, overdraft_limit, used_overdraft, tenure_months, status)
VALUES 
('SB-1012-IN', 'Anish Vyapari', 'anish.vyapari@dypatil.edu', '+91 98201 12345', 'SAVINGS', 50000.00, 4.25, 2000.00, 0.00, 0.00, 0, 'ACTIVE'),
('CA-9080-CORP', 'Vyapari Tech Solutions LLP', 'finance@vyaparitech.com', '+91 22 2770 9999', 'CURRENT', 100000.00, 0.00, 0.00, 50000.00, 0.00, 0, 'ACTIVE'),
('SB-3045-EXT', 'Kavita Rao', 'kavita.rao@rait.ac.in', '+91 98334 56789', 'SAVINGS', 15000.00, 4.00, 2000.00, 0.00, 0.00, 0, 'ACTIVE'),
('FD-7001-INV', 'Prof. Ramesh Patil', 'r.patil@dypatil.edu', '+91 98210 77777', 'FIXED_DEPOSIT', 250000.00, 7.50, 0.00, 0.00, 0.00, 12, 'ACTIVE');

-- 2. Insert Account Opening Transactions
INSERT INTO TRANSACTIONS (txn_id, account_number, txn_type, amount, balance_after, reference_note, counterparty_account, timestamp)
VALUES 
('TXN-0001', 'SB-1012-IN', 'OPEN', 50000.00, 50000.00, 'Initial Account Opening Balance', NULL, datetime('now', '-5 days')),
('TXN-0002', 'CA-9080-CORP', 'OPEN', 100000.00, 100000.00, 'Corporate Working Capital Deposit', NULL, datetime('now', '-5 days')),
('TXN-0003', 'SB-3045-EXT', 'OPEN', 15000.00, 15000.00, 'Savings Account Opening Balance', NULL, datetime('now', '-5 days')),
('TXN-0004', 'FD-7001-INV', 'OPEN', 250000.00, 250000.00, 'Term Deposit Principal Investment (12 Months @ 7.5% p.a.)', NULL, datetime('now', '-5 days'));

-- 3. Insert Beneficiaries for Easy Inter-Account Wire Transfers
INSERT INTO BENEFICIARIES (source_account, beneficiary_account, beneficiary_name, bank_ifsc, max_limit)
VALUES 
('SB-1012-IN', 'SB-3045-EXT', 'Kavita Rao', 'SBTMS00025CA', 100000.00),
('SB-1012-IN', 'CA-9080-CORP', 'Vyapari Tech Solutions LLP', 'SBTMS00025CA', 250000.00),
('CA-9080-CORP', 'SB-1012-IN', 'Anish Vyapari (Director Dividend)', 'SBTMS00025CA', 500000.00);

-- 4. Insert Initial Audit Logs
INSERT INTO AUDIT_LOGS (action, account_number, details, status, ip_address, timestamp)
VALUES 
('SYSTEM_INITIALIZE', NULL, 'Core Banking DBMS & SQL persistence engine initialized with relational schema', 'SUCCESS', '127.0.0.1', datetime('now', '-5 days')),
('ACCOUNT_CREATION', 'SB-1012-IN', 'Provisioned Savings Account for Anish Vyapari with interest rate 4.25% p.a.', 'SUCCESS', '127.0.0.1', datetime('now', '-5 days')),
('ACCOUNT_CREATION', 'CA-9080-CORP', 'Provisioned Current Account for Vyapari Tech Solutions LLP with OD limit Rs. 50,000.00', 'SUCCESS', '127.0.0.1', datetime('now', '-5 days')),
('ACCOUNT_CREATION', 'SB-3045-EXT', 'Provisioned Savings Account for Kavita Rao with interest rate 4.00% p.a.', 'SUCCESS', '127.0.0.1', datetime('now', '-5 days')),
('ACCOUNT_CREATION', 'FD-7001-INV', 'Provisioned Fixed Deposit Account for Prof. Ramesh Patil (12 Months @ 7.50% p.a.)', 'SUCCESS', '127.0.0.1', datetime('now', '-5 days'));

-- 5. Insert System Metrics
INSERT INTO SYSTEM_METRICS (metric_key, metric_value)
VALUES 
('SYSTEM_NAME', 'Smart Banking Transaction & Account Portfolio Management System (SB-TMS)'),
('SYSTEM_VERSION', '2.5.0-CAPSTONE-ENTERPRISE'),
('INSTITUTION', 'Ramrao Adik Institute of Technology, Nerul'),
('ACADEMIC_YEAR', '2026-2027'),
('DATABASE_ENGINE', 'SQLite 3 via JDBC / Relational DBMS');
