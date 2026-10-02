-- ==============================================================================
-- SMART BANKING TRANSACTION & ACCOUNT PORTFOLIO MANAGEMENT SYSTEM (SB-TMS)
-- CAPSTONE PROJECT - RELATIONAL DBMS SQL SCHEMA
-- Course: SBL - Object Oriented Programming in Java & DBMS
-- Candidate: Anish Vyapari | Roll No: 25CA1012 | PRN: DY25ENGU0AIM012
-- Institution: Ramrao Adik Institute of Technology (RAIT), Nerul, Navi Mumbai
-- ==============================================================================

-- 1. ACCOUNTS TABLE (Core entity supporting polymorphic account types)
CREATE TABLE IF NOT EXISTS ACCOUNTS (
    account_number VARCHAR(30) PRIMARY KEY,
    holder_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    account_type VARCHAR(20) NOT NULL CHECK(account_type IN ('SAVINGS', 'CURRENT', 'FIXED_DEPOSIT')),
    balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    interest_rate DECIMAL(5, 2) DEFAULT 0.00,
    min_balance DECIMAL(15, 2) DEFAULT 0.00,
    overdraft_limit DECIMAL(15, 2) DEFAULT 0.00,
    used_overdraft DECIMAL(15, 2) DEFAULT 0.00,
    tenure_months INTEGER DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK(status IN ('ACTIVE', 'DORMANT', 'FROZEN', 'CLOSED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. TRANSACTIONS TABLE (Relational Ledger replacing in-memory 2D array)
CREATE TABLE IF NOT EXISTS TRANSACTIONS (
    txn_id VARCHAR(30) PRIMARY KEY,
    account_number VARCHAR(30) NOT NULL,
    txn_type VARCHAR(30) NOT NULL CHECK(txn_type IN ('OPEN', 'DEP', 'DEP-OD-REPAY', 'WDL', 'WDL-OD', 'WDL-LIQUIDATE', 'TRANSFER-OUT', 'TRANSFER-IN', 'INT', 'INT-COMPOUND', 'OD-LEVY')),
    amount DECIMAL(15, 2) NOT NULL,
    balance_after DECIMAL(15, 2) NOT NULL,
    reference_note TEXT,
    counterparty_account VARCHAR(30),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_number) REFERENCES ACCOUNTS(account_number) ON DELETE CASCADE
);

-- 3. BENEFICIARIES TABLE (Inter-account transfer recipient registry)
CREATE TABLE IF NOT EXISTS BENEFICIARIES (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    source_account VARCHAR(30) NOT NULL,
    beneficiary_account VARCHAR(30) NOT NULL,
    beneficiary_name VARCHAR(100) NOT NULL,
    bank_ifsc VARCHAR(20) NOT NULL,
    max_limit DECIMAL(15, 2) DEFAULT 100000.00,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (source_account) REFERENCES ACCOUNTS(account_number) ON DELETE CASCADE
);

-- 4. AUDIT_LOGS TABLE (Security & Compliance audit trail)
CREATE TABLE IF NOT EXISTS AUDIT_LOGS (
    log_id INTEGER PRIMARY KEY AUTOINCREMENT,
    action VARCHAR(50) NOT NULL,
    account_number VARCHAR(30),
    details TEXT NOT NULL,
    status VARCHAR(20) NOT NULL CHECK(status IN ('SUCCESS', 'FAILED', 'WARNING', 'BLOCKED')),
    ip_address VARCHAR(45) DEFAULT '127.0.0.1',
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. SYSTEM_METRICS TABLE (Key-value metrics store)
CREATE TABLE IF NOT EXISTS SYSTEM_METRICS (
    metric_key VARCHAR(50) PRIMARY KEY,
    metric_value TEXT NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- INDEXES FOR QUERY OPTIMIZATION
CREATE INDEX IF NOT EXISTS idx_txn_account ON TRANSACTIONS(account_number);
CREATE INDEX IF NOT EXISTS idx_txn_timestamp ON TRANSACTIONS(timestamp);
CREATE INDEX IF NOT EXISTS idx_audit_timestamp ON AUDIT_LOGS(timestamp);
CREATE INDEX IF NOT EXISTS idx_beneficiary_source ON BENEFICIARIES(source_account);
