# Smart Banking Transaction and Account Portfolio Management System (SB-TMS)
### Enterprise Capstone Project: Object-Oriented Programming (Java) & Relational DBMS

[![Java 21](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://adoptium.net/)
[![SQLite](https://img.shields.io/badge/DBMS-SQLite%203.45-blue.svg)](https://sqlite.org/)
[![JDBC](https://img.shields.io/badge/Persistence-JDBC%20ACID-success.svg)](https://docs.oracle.com/en/java/javase/21/docs/api/java.sql/package-summary.html)
[![Web](https://img.shields.io/badge/Frontend-HTML5%20%7C%20CSS3%20%7C%20ES6+-informational.svg)](http://localhost:8080)
[![Institution](https://img.shields.io/badge/Institution-RAIT%20Nerul-darkblue.svg)](http://dypatil.edu/rait)

---

## 🎓 Academic Profile
- **Candidate Name:** Anish Vyapari
- **PRN / Student ID:** `DY25ENGU0AIM012`
- **Roll Number:** `25CA1012`
- **Batch:** `A/A1`
- **Department:** Department of Computer Engineering
- **Institution:** Ramrao Adik Institute of Technology (RAIT), Nerul, Navi Mumbai
- **Course:** SBL – Object-Oriented Programming in Java & Relational DBMS
- **Academic Year:** 2026

---

## 📌 Project Overview
The **Smart Banking Transaction and Account Portfolio Management System (SB-TMS)** is an industrial-grade enterprise capstone application that transforms **SBL OOP Java Experiment 11** from an academic in-memory prototype into a resilient, multi-tier software system. 

It synthesizes object-oriented design patterns with **Relational Database Management System (DBMS)** persistence via **SQLite and JDBC**, strict **ACID transactional atomicity**, domain boundary invariant enforcement via custom checked exceptions, an embedded zero-dependency Java SE HTTP REST server, and a modern financial web dashboard.

---

## 🚀 1-Click Launch (Zero Configuration)

### 🪟 Windows (Recommended)
Simply **double-click** the launcher script in the root directory:
```cmd
run.bat
```
*What this script automatically does:*
1. Locates OpenJDK 17/21 (searches local JDK paths, Adoptium, Microsoft OpenJDK, Oracle, or system PATH).
2. Verifies and auto-bundles the SQLite JDBC Driver (`sqlite-jdbc.jar`) and logging libraries.
3. Automatically compiles all pure Java classes into bytecode.
4. Auto-clears port `8080` if previously occupied.
5. Launches the backend embedded server and automatically opens `http://localhost:8080/index.html` in your default browser.

### 🛑 Stopping the Server
Double-click:
```cmd
stop.bat
```

### 💻 Standalone Interactive CLI (Experiment 11 Terminal Mode)
Double-click:
```cmd
project\run-cli.bat
```

### 🐧 Linux & macOS
```bash
chmod +x run.sh project/run.sh
./run.sh
```

---

## 🏛️ System Architecture

SB-TMS is architected as an enterprise-grade 5-tier system:
1. **Client Presentation Tier:** Modern responsive web dashboard featuring authentic debit card visualizations, real-time KPI metrics, interactive modals for instant **Deposit**, **Withdrawal**, and **Wire Transfers**, an authentic 2D passbook statement generator, and a live SQL query console.
2. **Java SE Embedded REST Gateway:** Zero-dependency `com.sun.net.httpserver.HttpServer` listening on port `8080`, providing RESTful JSON endpoints (`/api/accounts`, `/api/transactions/*`, `/api/ledger/*`, `/api/batch/*`, `/api/sql`, `/api/analytics`, `/api/audit-logs`).
3. **Object-Oriented Domain Tier:** Deep class hierarchy rooted in abstract base class `Account`, implementing `TransactionProcessor`, `Auditable`, and `AccountDAO` contracts, specialized into `SavingsAccount`, `CurrentAccount`, and `FixedDepositAccount`.
4. **JDBC Data Access Tier:** Data Access Object pattern (`AccountDAOImpl`) executing parameterized `PreparedStatements` to guarantee zero SQL injection vulnerability.
5. **Relational SQLite Storage Engine:** ACID-compliant relational persistence across `ACCOUNTS`, `TRANSACTIONS`, `BENEFICIARIES`, `AUDIT_LOGS`, and `SYSTEM_METRICS`.

---

## 🔑 Core Object-Oriented Principles Implemented

| OOP Principle | Implementation in SB-TMS | File Reference |
| :--- | :--- | :--- |
| **Encapsulation** | Private account balances, overdraft state, and audit logs with strict accessor/mutator invariants. | [`Account.java`](project/backend/src/com/sbtms/model/Account.java) |
| **Inheritance** | Base class `Account` extended by `SavingsAccount`, `CurrentAccount`, and `FixedDepositAccount`. | [`SavingsAccount.java`](project/backend/src/com/sbtms/model/SavingsAccount.java) |
| **Polymorphism** | Method overloading (`deposit(amt)`, `deposit(amt, memo)`) & dynamic dispatch (`applyPeriodicInterest()`). | [`CurrentAccount.java`](project/backend/src/com/sbtms/model/CurrentAccount.java) |
| **Abstraction** | Contract specifications via `TransactionProcessor`, `Auditable`, and `AccountDAO` interfaces. | [`TransactionProcessor.java`](project/backend/src/com/sbtms/interfaces/TransactionProcessor.java) |
| **Custom Exceptions** | `InsufficientFundsException` (ERR-BANK-001) & `OverdraftExceededException` (ERR-BANK-002). | [`BankingException.java`](project/backend/src/com/sbtms/exception/BankingException.java) |
| **2D Arrays** | Preserved `String[50][4]` ledger array for printing authentic Experiment 11 passbook statements. | [`CLIConsole.java`](project/backend/src/com/sbtms/cli/CLIConsole.java) |

---

## ⚡ Banking Domain Rules Enforced

1. **Savings Account:**
   - Statutory Minimum Balance: **Rs. 2,000.00**.
   - Attempting to withdraw funds that would breach this reserve raises `InsufficientFundsException` (ERR-BANK-001) and rejects the transaction.
   - Earns periodic interest at **4.25% p.a.** calculated quarterly.
2. **Current Account:**
   - Approved Commercial Overdraft Ceiling: **Rs. 50,000.00**.
   - Withdrawing beyond available cash draws from overdraft; exceeding the overdraft ceiling raises `OverdraftExceededException` (ERR-BANK-002).
   - Incurs a **3.0% quarterly fee** on active overdraft balance.
3. **Fixed Deposit Account:**
   - Term investment compounding at **7.50% p.a.** for a 12-month tenure.
   - Premature liquidation enforces statutory penalty deductions.
4. **Inter-Account Wire Transfer (ACID):**
   - Atomic multi-statement execution (`conn.setAutoCommit(false)`, `commit()`, `rollback()`).
   - If an error occurs midway through transfer, both accounts are immediately rolled back with zero balance drift.

---

## 📁 Repository Directory Structure

```text
dbms-capstone/
├── run.bat                               # Root 1-click Windows launcher
├── stop.bat                              # Clean port 8080 shutdown script
├── run.sh                                # Root Linux / macOS launcher script
├── README.md                             # Repository technical presentation
├── Capstone Project Report.docx          # Official college-formatted Capstone Report (18 Pages)
├── Capstone_Project_Documentation.pdf    # Publication-grade technical PDF report
├── SBL_OOP_Java_Lab_Record_Anish_Vyapari.pdf # Reference lab record manual
└── project/
    ├── run.bat                           # Project Windows launcher
    ├── setup.bat                         # Project compiler and build script
    ├── run-cli.bat                       # Standalone CLI console runner
    ├── stop.bat                          # Port 8080 shutdown utility
    ├── run.sh                            # Project Linux/macOS launcher
    ├── backend/
    │   ├── src/com/sbtms/
    │   │   ├── Main.java                 # Master application entry point
    │   │   ├── model/                    # Domain classes (Account, Savings, Current, FD)
    │   │   ├── interfaces/               # Abstract contracts (TransactionProcessor, Auditable)
    │   │   ├── exception/                # Domain-specific checked exceptions
    │   │   ├── db/                       # DatabaseManager, AccountDAOImpl (JDBC, SQL)
    │   │   ├── service/                  # BankingService (ACID Transfers), AnalyticsService
    │   │   ├── web/                      # Embedded Java HTTP REST Server & JSON parser
    │   │   └── cli/                      # Standalone CLI interface (Experiment 11 mode)
    │   ├── lib/                          # Pre-bundled drivers (sqlite-jdbc, slf4j)
    │   └── bin/                          # Pre-compiled JVM bytecode (.class files)
    ├── database/
    │   ├── schema.sql                    # Normalized Relational DDL tables
    │   ├── seed.sql                      # Experiment 11 baseline seed data
    │   └── sbtms_bank.db                 # Active SQLite relational database
    ├── frontend/
    │   ├── index.html                    # Modern responsive banking dashboard
    │   ├── css/styles.css                # Dark/light theme design system
    │   ├── css/passbook.css              # Bank passbook statement styling
    │   ├── js/app.js                     # Application frontend controller
    │   ├── js/transactions.js            # Transaction & modal submission handlers
    │   ├── js/passbook.js                # 2D passbook statement generator
    │   └── js/sql-console.js             # Live interactive SQL inspection terminal
    ├── docs/                             # Architecture diagrams, ER schema, flowcharts
    └── screenshots/                      # 7 high-resolution application screenshots
```

---

## 📄 Academic Deliverables
- **DOCX Report:** [`Capstone Project Report.docx`](Capstone%20Project%20Report.docx) (Compliant with official RAIT template, exclusively authored by Anish Vyapari, containing all chapters, 5 diagrams, 4 code listings, and 7 live screenshots).
- **PDF Report:** [`Capstone_Project_Documentation.pdf`](Capstone_Project_Documentation.pdf) (15-page publication-grade PDF specification).

---

## 👨‍💻 Author
**Anish Vyapari**  
*PRN: DY25ENGU0AIM012 | Roll No: 25CA1012 | Batch: A/A1*  
*Department of Computer Engineering, Ramrao Adik Institute of Technology, Nerul*  
*Email:* `hyperskyiez@gmail.com`
