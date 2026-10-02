# SMART BANKING TRANSACTION & ACCOUNT PORTFOLIO MANAGEMENT SYSTEM (SB-TMS)
### Enterprise Capstone Project: Object-Oriented Java & Relational DBMS Persistence

**Course:** SBL – Object Oriented Programming in Java (Semester 3)  
**Department:** Department of Computer Engineering  
**Institution:** Ramrao Adik Institute of Technology (RAIT), Nerul, Navi Mumbai (D. Y. Patil Deemed to be University)  
**Candidate Name:** **Anish Vyapari**  
**Roll No:** **25CA1012** | **PRN / USN:** **DY25ENGU0AIM012** | **Batch:** **A / A1**  
**Academic Year:** 2026–2027  

---

## 1. Project Overview & Problem Statement

Modern banking and enterprise financial systems demand high reliability, rigorous enforcement of statutory balance rules, resilient overdraft credit facilities, and non-repudiation audit tracking. 

The **Smart Banking Transaction and Account Portfolio Management System (SB-TMS)** is a full-featured, enterprise-grade banking platform that evolves directly from **SBL OOP Java Experiment 11**. It bridges classical object-oriented software engineering principles with industrial relational DBMS persistence (SQLite via JDBC) and an interactive web interface.

### Core Objectives
1. **Model Banking Portfolios with Pure OOP:** Represent distinct financial instruments (`SavingsAccount`, `CurrentAccount`, `FixedDepositAccount`) inheriting from an abstract base class `Account` and implementing strict interface contracts (`TransactionProcessor`, `Auditable`, `AccountDAO`).
2. **Enforce Domain Invariants via Custom Exceptions:** Trap minimum balance breaches (`InsufficientFundsException` - `ERR-BANK-001`) and commercial overdraft ceilings (`OverdraftExceededException` - `ERR-BANK-002`).
3. **Execute Atomic Inter-Account Wire Transfers:** Guarantee ACID transaction properties (Atomicity, Consistency, Isolation, Durability) using JDBC transaction demarcation (`conn.setAutoCommit(false)`, `conn.commit()`, and `conn.rollback()`).
4. **Demonstrate Runtime Dynamic Dispatch:** Perform automated end-of-quarter servicing across heterogeneous accounts via polymorphic method dispatch (`applyPeriodicInterest()`), accruing savings interest (@4.25% p.a.), commercial overdraft utilization fees (3.0%), and term deposit compounding yield.
5. **Relational DBMS Persistence:** Transition from Experiment 11's in-memory 2D array (`String[50][4]`) to a relational SQL schema (`ACCOUNTS`, `TRANSACTIONS`, `BENEFICIARIES`, `AUDIT_LOGS`, `SYSTEM_METRICS`) with full PreparedStatements protection against SQL injection.
6. **Zero-Dependency Lightweight Web Architecture:** Provide a rich web dashboard served directly by Java SE's built-in `com.sun.net.httpserver.HttpServer`, eliminating heavy third-party containers and allowing 1-click execution on any normal Windows laptop.

---

## 2. Experiment 11 → Capstone Evolution Matrix

| Architectural Dimension | SBL OOP Java Experiment 11 (Baseline) | SB-TMS Capstone Project (Enterprise Expansion) |
| :--- | :--- | :--- |
| **Persistence Layer** | Transient in-memory 2D array (`String[50][4]`) | Relational SQLite DBMS (`sbtms_bank.db`) with Foreign Keys, Indexes & ACID Transactions |
| **Account Hierarchy** | `SavingsAccount`, `CurrentAccount` | Extended with `FixedDepositAccount` (tenure, compound interest, premature liquidation penalties) |
| **Database Operations** | Local variable manipulation | DAO Pattern (`AccountDAOImpl`), PreparedStatements, and Interactive Web SQL Inspector |
| **Transaction Boundaries** | In-memory deduction and addition | Atomic JDBC Transaction rollback upon failure; zero balance leakage |
| **User Interface** | Standard Console Terminal output | Modern Responsive Web Dashboard + Physical-style Passbook Printer + Standalone Interactive CLI |
| **Audit & Security** | Simple `System.out.println` | Persistent `AUDIT_LOGS` table recording IP address, action code, timestamp, and status |
| **Web Service Gateway** | None (Local execution only) | Embedded RESTful JSON Web Server (`HttpServerApp`) with CORS support |

---

## 3. Object-Oriented Programming (OOP) Implementation

### A. Encapsulation
All mutable state attributes (e.g., `balance`, `usedOverdraft`, `annualInterestRate`, `accountNumber`) are declared `protected` or `private`. Invariants are strictly enforced through public methods (`deposit`, `withdraw`, `transfer`), preventing unauthorized direct modification.

### B. Inheritance & The `super` Keyword
The abstract class `Account` encapsulates shared identity attributes (`accountNumber`, `holderName`, `email`, `phone`, `balance`). Derived classes (`SavingsAccount`, `CurrentAccount`, `FixedDepositAccount`) invoke base constructors via `super(accountNumber, holderName, initialDeposit)` and inherit concrete utility methods while providing domain-specific specializations.

### C. Polymorphism
- **Compile-Time (Method Overloading):**
  - `deposit(double amount)`: Standard cash deposit.
  - `deposit(double amount, String referenceNote)`: Overloaded deposit with an explicit transaction memo.
  - Multiple constructor signatures in `Account`, `SavingsAccount`, and `CurrentAccount`.
- **Runtime (Dynamic Method Dispatch):**
  - `acc.applyPeriodicInterest()`: Executed across an array or collection of `Account` references. At runtime, the JVM dynamically invokes `SavingsAccount.applyPeriodicInterest()` (interest credit), `CurrentAccount.applyPeriodicInterest()` (overdraft utilization levy), or `FixedDepositAccount.applyPeriodicInterest()` (compound yield) based on the actual object type.

### D. Abstraction & Interfaces
- `TransactionProcessor`: Behavioral contract for monetary operations.
- `Auditable`: Contract for statement generation and audit summaries.
- `AccountDAO`: Contract for relational DBMS interactions.

### E. Robust Exception Handling
Checked exceptions enforce domain rules:
- `InsufficientFundsException`: Raised when a Savings Account withdrawal leaves a balance below the statutory minimum operating balance of **Rs. 2,000.00**.
- `OverdraftExceededException`: Raised when a Current Account commercial withdrawal exceeds the approved overdraft limit of **Rs. 50,000.00**.
- `AccountNotFoundException` & `InvalidTransactionException`: Handle invalid account inputs and negative/zero amounts.

---

## 4. Relational DBMS Schema & Architecture

The relational database is structured in `project/database/schema.sql`:

1. **`ACCOUNTS` Table:**
   - `account_number VARCHAR(30) PRIMARY KEY`
   - `holder_name VARCHAR(100) NOT NULL`
   - `account_type VARCHAR(20) CHECK IN ('SAVINGS', 'CURRENT', 'FIXED_DEPOSIT')`
   - `balance DECIMAL(15,2)`
   - `interest_rate`, `min_balance`, `overdraft_limit`, `used_overdraft`, `tenure_months`
   - `status VARCHAR(20)`, `created_at TIMESTAMP`

2. **`TRANSACTIONS` Table:**
   - `txn_id VARCHAR(30) PRIMARY KEY`
   - `account_number VARCHAR(30) FOREIGN KEY REFERENCES ACCOUNTS(account_number)`
   - `txn_type VARCHAR(30)` (`OPEN`, `DEP`, `WDL`, `WDL-OD`, `TRANSFER-OUT`, `TRANSFER-IN`, `INT`, `OD-LEVY`)
   - `amount DECIMAL(15,2)`, `balance_after DECIMAL(15,2)`
   - `reference_note TEXT`, `counterparty_account VARCHAR(30)`, `timestamp TIMESTAMP`

3. **`BENEFICIARIES` Table:**
   - `id INTEGER PRIMARY KEY AUTOINCREMENT`
   - `source_account VARCHAR(30) REFERENCES ACCOUNTS(account_number)`
   - `beneficiary_account`, `beneficiary_name`, `bank_ifsc`, `max_limit`

4. **`AUDIT_LOGS` Table:**
   - `log_id INTEGER PRIMARY KEY AUTOINCREMENT`
   - `action`, `account_number`, `details`, `status`, `ip_address`, `timestamp`

---

## 5. How to Run the Capstone Project

### Quick Start (Normal Windows Laptop - 1-Click Launch)
1. Navigate into the `project` folder.
2. Double-click **`run.bat`**.
3. The script automatically:
   - Verifies the Java runtime (OpenJDK 17+).
   - Verifies bundled JDBC drivers.
   - Compiles any unbuilt Java source files.
   - Starts the embedded Java HTTP Server on `port 8080`.
   - Automatically opens your default web browser to: **`http://localhost:8080/index.html`**.

### Standalone Console Demonstration (Experiment 11 Mode)
To demonstrate the pure terminal OOP execution to the professor:
1. Double-click **`run-cli.bat`**.
2. The console runs the automated verification of Experiment 11 (accounts provisioning, deposits, withdrawals, wire transfer, exception boundary trapping, end-of-quarter dynamic dispatch, and 2D array passbook statements).
3. It then presents an interactive menu (`1. View Accounts`, `2. Deposit`, `3. Withdraw`, `4. Wire Transfer`, `5. Quarter-End Servicing`, `6. Passbook`, `7. Reset Seed`, `8. Exit`).

### Stopping the Application
- Close the command prompt window, OR
- Double-click **`stop.bat`** to terminate any lingering processes on port 8080.

---

## 6. Project Directory Structure

```
project/
├── backend/
│   ├── bin/                 # Compiled Java .class bytecode
│   ├── lib/                 # SQLite JDBC & SLF4J Driver Jars (self-contained)
│   └── src/
│       └── com/sbtms/
│           ├── model/       # Account, SavingsAccount, CurrentAccount, FixedDepositAccount, Transaction
│           ├── interfaces/  # TransactionProcessor, Auditable, AccountDAO
│           ├── exception/   # InsufficientFundsException, OverdraftExceededException, BankingException
│           ├── db/          # DatabaseManager (JDBC lifecycle, ACID), AccountDAOImpl
│           ├── service/     # BankingService (Business logic), AnalyticsService (Portfolio metrics)
│           ├── web/         # HttpServerApp (Java SE embedded server), JsonHelper
│           ├── cli/         # CLIConsole (Experiment 11 console demo)
│           └── Main.java    # Unified application entry point
├── database/
│   ├── schema.sql           # Complete relational DBMS DDL schema
│   ├── seed.sql             # Benchmark seed dataset matching Exp 11
│   └── sbtms_bank.db        # SQLite relational database file
├── frontend/
│   ├── index.html           # Comprehensive banking dashboard & SQL inspector
│   ├── css/
│   │   ├── styles.css       # Design tokens, dark/light theme, bank card styling
│   │   └── passbook.css     # Physical bank passbook layout & print formatting
│   ├── js/
│   │   ├── app.js           # Main controller, state management, toast alerts
│   │   ├── transactions.js  # Deposit, withdrawal, and wire transfer handlers
│   │   ├── passbook.js      # Passbook 2D ledger renderer & CSV exporter
│   │   └── sql-console.js   # Live interactive SQL query visualizer
│   └── assets/              # Bank logo, smart chip SVG, icons
├── docs/
│   ├── Capstone Project Report.docx     # Academic Capstone Report (from official template)
│   ├── Capstone_Project_Documentation.pdf # Publication-grade Technical PDF Documentation
│   ├── architecture_diagram.png         # Multi-tier system architecture
│   ├── class_diagram.png                # UML class hierarchy
│   ├── er_diagram.png                   # Relational database ER schema
│   ├── sequence_diagram.png             # Inter-account transfer sequence
│   └── transaction_flowchart.png        # Transaction logic flowchart
├── screenshots/             # 7 Live screenshots captured via Playwright automation
├── run.bat                  # 1-Click Master Windows Launcher
├── setup.bat                # Build and dependency configuration script
├── run-cli.bat              # Standalone CLI console launcher
├── stop.bat                 # Graceful server shutdown utility
└── README.md                # Complete documentation guide
```

---

## 7. Author Information

- **Student:** Anish Vyapari
- **Roll Number:** 25CA1012
- **PRN / USN:** DY25ENGU0AIM012
- **Division / Batch:** A / A1
- **Department:** Department of Computer Engineering
- **Institution:** Ramrao Adik Institute of Technology (RAIT), Nerul, Navi Mumbai
- **University:** D. Y. Patil Deemed to be University
