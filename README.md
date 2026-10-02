# Smart Banking Transaction and Account Portfolio Management System (SB-TMS)
### Enterprise Capstone Project: Object-Oriented Programming (Java) & Relational DBMS

[![Java 17+](https://img.shields.io/badge/Java-17%2B%20LTS-orange.svg)](https://adoptium.net/)
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

## 🚀 One-Click Launch (Works on ANY Windows PC — No Setup Required)

### Step 1: Clone the Repository
```cmd
git clone https://github.com/Lanthanode/capstone-OOP-java-SBL-1-LAB.git
cd capstone-OOP-java-SBL-1-LAB
```

### Step 2: Double-Click `START.bat`
That's it. The script handles **everything** automatically:

| What It Does | Details |
|:---|:---|
| ✅ **Finds Java** | Searches PATH, JAVA_HOME, Program Files, Registry — all major JDK distributions |
| ✅ **Downloads Java** | If no JDK is found, auto-downloads a portable OpenJDK 21 (~200 MB, one-time) |
| ✅ **Downloads Drivers** | SQLite JDBC, SLF4J logging libraries — downloaded only if missing |
| ✅ **Compiles Code** | Recompiles all Java source files fresh (prevents cross-machine bytecode conflicts) |
| ✅ **Clears Port** | Kills any process already using port 8080 |
| ✅ **Starts Server** | Launches the Java HTTP server on localhost:8080 |
| ✅ **Opens Browser** | Waits for server to be healthy, then auto-opens the dashboard |
| ✅ **2nd Run Detection** | Re-running skips downloads, recompiles fresh, and launches instantly |

### 🛑 Stopping the Server
- **Close the terminal window**, OR
- Double-click `STOP.bat`

### 💻 Standalone Interactive CLI (Experiment 11 Terminal Mode)
```cmd
project\run-cli.bat
```

### 🐧 Linux & macOS
```bash
chmod +x run.sh project/run.sh
./run.sh
```

---

## 📌 Project Overview
The **Smart Banking Transaction and Account Portfolio Management System (SB-TMS)** is an industrial-grade enterprise capstone application that transforms **SBL OOP Java Experiment 11** from an academic in-memory prototype into a resilient, multi-tier software system. 

It synthesizes object-oriented design patterns with **Relational Database Management System (DBMS)** persistence via **SQLite and JDBC**, strict **ACID transactional atomicity**, domain boundary invariant enforcement via custom checked exceptions, an embedded zero-dependency Java SE HTTP REST server, and a modern financial web dashboard.

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
capstone-OOP-java-SBL-1-LAB/
├── START.bat                             # ⭐ MAIN LAUNCHER — double-click this!
├── STOP.bat                              # Stop the running server
├── run.bat                               # Legacy launcher (redirects to START.bat)
├── stop.bat                              # Legacy stop (redirects to STOP.bat)
├── run.sh                                # Linux / macOS launcher
├── README.md                             # This file
├── .gitignore                            # Git exclusions
├── Capstone Project Report.docx          # Official RAIT-formatted Capstone Report
├── Capstone_Project_Documentation.pdf    # Technical PDF report
├── SBL_OOP_Java_Lab_Record_Anish_Vyapari.pdf
└── project/
    ├── run.bat                           # Alternative launcher from project dir
    ├── setup.bat                         # Manual build script
    ├── run-cli.bat                       # CLI console runner
    ├── stop.bat                          # Port shutdown utility
    ├── run.sh                            # Linux/macOS launcher
    ├── backend/
    │   ├── src/com/sbtms/
    │   │   ├── Main.java                 # Master application entry point
    │   │   ├── model/                    # Domain classes (Account, Savings, Current, FD)
    │   │   ├── interfaces/               # Abstract contracts (TransactionProcessor, Auditable)
    │   │   ├── exception/                # Domain-specific checked exceptions
    │   │   ├── db/                       # DatabaseManager, AccountDAOImpl (JDBC, SQL)
    │   │   ├── service/                  # BankingService (ACID Transfers), AnalyticsService
    │   │   ├── web/                      # Embedded Java HTTP Server & JSON parser
    │   │   └── cli/                      # Standalone CLI interface (Experiment 11 mode)
    │   └── lib/                          # Auto-downloaded drivers (sqlite-jdbc, slf4j)
    ├── database/
    │   ├── schema.sql                    # Normalized Relational DDL tables
    │   └── seed.sql                      # Experiment 11 baseline seed data
    └── frontend/
        ├── index.html                    # Modern responsive banking dashboard
        ├── css/                          # Dark/light theme design system
        ├── js/                           # Application controllers
        └── assets/                       # Static assets
```

---

## 🔧 Troubleshooting

| Problem | Solution |
|:---|:---|
| `START.bat` closes immediately | Right-click → "Run as Administrator" |
| Java not found & download fails | Install JDK manually from [adoptium.net](https://adoptium.net/) |
| Port 8080 already in use | `STOP.bat` will kill the old process, or run `START.bat` again |
| Page shows 404 | Make sure you're accessing `http://localhost:8080/index.html` |
| Antivirus blocks download | Temporarily disable or whitelist `curl.exe` and `powershell.exe` |

---

## 📄 Academic Deliverables
- **DOCX Report:** [`Capstone Project Report.docx`](Capstone%20Project%20Report.docx)
- **PDF Report:** [`Capstone_Project_Documentation.pdf`](Capstone_Project_Documentation.pdf)

---

## 👨‍💻 Author
**Anish Vyapari**  
*PRN: DY25ENGU0AIM012 | Roll No: 25CA1012 | Batch: A/A1*  
*Department of Computer Engineering, Ramrao Adik Institute of Technology, Nerul*  
*Email:* `hyperskyiez@gmail.com`
