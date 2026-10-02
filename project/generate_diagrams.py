import os
from PIL import Image, ImageDraw, ImageFont

os.makedirs("docs", exist_ok=True)

def get_font(size, bold=False):
    # Try common Windows fonts
    fonts = ["arialbd.ttf" if bold else "arial.ttf", "segoeuib.ttf" if bold else "segoeui.ttf", "calibrib.ttf" if bold else "calibri.ttf"]
    for f in fonts:
        try:
            return ImageFont.truetype(f, size)
        except:
            continue
    return ImageFont.load_default()

# -------------------------------------------------------------
# 1. ARCHITECTURE DIAGRAM
# -------------------------------------------------------------
def make_architecture_diagram():
    w, h = 1200, 750
    img = Image.new("RGB", (w, h), "#0F172A")
    draw = ImageDraw.Draw(img)

    f_title = get_font(26, bold=True)
    f_sub = get_font(15, bold=False)
    f_box_h = get_font(18, bold=True)
    f_box_p = get_font(13, bold=False)

    # Title Banner
    draw.text((w//2, 35), "SB-TMS: Multi-Tier Enterprise System Architecture", font=f_title, fill="#F8FAFC", anchor="mm")
    draw.text((w//2, 70), "SBL OOP Java & Relational DBMS Capstone | Candidate: Anish Vyapari (25CA1012 | DY25ENGU0AIM012 | A/A1)", font=f_sub, fill="#94A3B8", anchor="mm")

    layers = [
        ("1. CLIENT INTERACTION LAYER (Frontend)", "#1E293B", "#3B82F6", 110, [
            "Modern Web Dashboard (HTML5, CSS3, ES6 JavaScript)",
            "Responsive Cards, Glassmorphism, Tabbed Navigation",
            "Passbook 2D Ledger Renderer & CSV/Print Exporters",
            "Interactive SQL Console & Live Query Visualizer"
        ]),
        ("2. API & GATEWAY LAYER (Java SE Embedded Server)", "#1E293B", "#10B981", 240, [
            "com.sun.net.httpserver.HttpServer (Zero-Container Overhead, Instant Boot)",
            "RESTful JSON Endpoints: /api/accounts, /api/transactions, /api/batch, /api/sql",
            "CORS Management, Request Dispatching & Static Resource Pipeline"
        ]),
        ("3. DOMAIN & BUSINESS LOGIC LAYER (Core OOP)", "#1E293B", "#F59E0B", 370, [
            "Polymorphic Banking Entities: Account -> SavingsAccount, CurrentAccount, FixedDepositAccount",
            "Interfaces: TransactionProcessor, Auditable, AccountDAO",
            "BankingService (ACID Transfers, Dynamic Method Dispatch Quarter-End Servicing)",
            "Custom Domain Exceptions: InsufficientFundsException, OverdraftExceededException"
        ]),
        ("4. DATA ACCESS & TRANSACTION MANAGEMENT (JDBC)", "#1E293B", "#8B5CF6", 500, [
            "AccountDAOImpl (Data Access Object Pattern)",
            "Connection Management, Transaction Rollback/Commit Boundary",
            "Parameterized SQL PreparedStatements (Zero SQL Injection Vulnerability)"
        ]),
        ("5. STORAGE & DATABASE ENGINE (Relational DBMS)", "#1E293B", "#06B6D4", 630, [
            "SQLite Relational DBMS (sbtms_bank.db)",
            "Tables: ACCOUNTS, TRANSACTIONS, BENEFICIARIES, AUDIT_LOGS, SYSTEM_METRICS",
            "Integrity Constraints: PRIMARY KEY, FOREIGN KEY, CHECK, Cascades & Indexes"
        ])
    ]

    for title, bg, border, y, points in layers:
        # Card
        draw.rounded_rectangle([60, y, w-60, y+100], radius=12, fill=bg, outline=border, width=2)
        # Header strip
        draw.rounded_rectangle([60, y, w-60, y+30], radius=10, fill=border)
        draw.text((75, y+15), title, font=f_box_h, fill="#FFFFFF", anchor="lm")

        # Content bullets
        bx = 80
        for i, pt in enumerate(points):
            draw.text((bx, y + 45 + (i * 18)), f"•  {pt}", font=f_box_p, fill="#E2E8F0")

    # Downward connectors
    for y in [210, 340, 470, 600]:
        draw.line([w//2, y, w//2, y+30], fill="#64748B", width=3)
        draw.polygon([(w//2, y+30), (w//2-6, y+20), (w//2+6, y+20)], fill="#64748B")

    img.save("docs/architecture_diagram.png")
    print("[Diagrams]: Generated docs/architecture_diagram.png")

# -------------------------------------------------------------
# 2. UML CLASS DIAGRAM
# -------------------------------------------------------------
def make_class_diagram():
    w, h = 1350, 950
    img = Image.new("RGB", (w, h), "#0F172A")
    draw = ImageDraw.Draw(img)

    f_title = get_font(26, bold=True)
    f_sub = get_font(15, bold=False)
    f_h = get_font(15, bold=True)
    f_t = get_font(12, bold=False)

    draw.text((w//2, 35), "SB-TMS: Unified Object-Oriented Class Hierarchy (UML)", font=f_title, fill="#F8FAFC", anchor="mm")
    draw.text((w//2, 70), "Inheritance, Abstract Classes, Interfaces & Dynamic Dispatch | Anish Vyapari (25CA1012)", font=f_sub, fill="#94A3B8", anchor="mm")

    # Interfaces Box (Top Left & Top Right)
    def draw_class(x1, y1, x2, y2, title, is_interface=False, is_abstract=False, attrs=[], methods=[], border_color="#3B82F6"):
        draw.rounded_rectangle([x1, y1, x2, y2], radius=8, fill="#1E293B", outline=border_color, width=2)
        head_text = ("<<interface>>\n" if is_interface else ("<<abstract>>\n" if is_abstract else "")) + title
        draw.text(((x1+x2)//2, y1+22), head_text, font=f_h, fill=border_color, anchor="mm", align="center")
        draw.line([x1, y1+44, x2, y1+44], fill=border_color, width=1)

        cy = y1 + 50
        for a in attrs:
            draw.text((x1+10, cy), a, font=f_t, fill="#CBD5E1")
            cy += 16
        draw.line([x1, cy+2, x2, cy+2], fill=border_color, width=1)
        cy += 8
        for m in methods:
            draw.text((x1+10, cy), m, font=f_t, fill="#E2E8F0")
            cy += 16

    # 1. Interface TransactionProcessor
    draw_class(50, 110, 420, 240, "TransactionProcessor", is_interface=True,
               attrs=[],
               methods=[
                   "+ deposit(amount: double): void",
                   "+ deposit(amount: double, note: String): void",
                   "+ withdraw(amount: double): void",
                   "+ transfer(recipient: Account, amount: double): void"
               ], border_color="#10B981")

    # 2. Interface Auditable
    draw_class(930, 110, 1300, 230, "Auditable", is_interface=True,
               attrs=[],
               methods=[
                   "+ printStatement(): void",
                   "+ generateAuditSummary(): String",
                   "+ getTransactionList(): List<Transaction>"
               ], border_color="#10B981")

    # 3. Abstract Class Account
    draw_class(450, 120, 900, 410, "Account", is_abstract=True,
               attrs=[
                   "# accountNumber: String",
                   "# holderName: String",
                   "# email, phone: String",
                   "# balance: double",
                   "# transactionLedger: String[100][4]",
                   "# txCount: int",
                   "# transactions: List<Transaction>"
               ],
               methods=[
                   "+ Account(accNo, name, deposit)",
                   "+ deposit(amount, note): void",
                   "+ transfer(recipient, amount): void",
                   "+ printStatement(): void",
                   "+ {abstract} withdraw(amount: double): void",
                   "+ {abstract} applyPeriodicInterest(): void",
                   "+ {abstract} getAccountType(): String"
               ], border_color="#F59E0B")

    # Connect Interfaces to Account
    draw.line([420, 175, 450, 175], fill="#10B981", width=2)
    draw.line([930, 175, 900, 175], fill="#10B981", width=2)

    # 4. Subclass SavingsAccount
    draw_class(50, 490, 430, 710, "SavingsAccount",
               attrs=[
                   "- annualInterestRate: double",
                   "+ {static} MIN_BALANCE: double = 2000.0"
               ],
               methods=[
                   "+ SavingsAccount(accNo, name, bal, rate)",
                   "+ withdraw(amount: double): void",
                   "+ applyPeriodicInterest(): void",
                   "+ generateAuditSummary(): String",
                   "+ getAvailableFunds(): double"
               ], border_color="#3B82F6")

    # 5. Subclass CurrentAccount
    draw_class(480, 490, 870, 725, "CurrentAccount",
               attrs=[
                   "- overdraftLimit: double",
                   "- usedOverdraft: double"
               ],
               methods=[
                   "+ CurrentAccount(accNo, name, bal, limit)",
                   "+ withdraw(amount: double): void",
                   "+ deposit(amount, note): void",
                   "+ applyPeriodicInterest(): void",
                   "+ generateAuditSummary(): String",
                   "+ getAvailableFunds(): double"
               ], border_color="#8B5CF6")

    # 6. Subclass FixedDepositAccount
    draw_class(920, 490, 1300, 710, "FixedDepositAccount",
               attrs=[
                   "- tenureMonths: int",
                   "- fixedInterestRate: double",
                   "- liquidated: boolean"
               ],
               methods=[
                   "+ FixedDepositAccount(accNo, name, ...)",
                   "+ withdraw(amount: double): void",
                   "+ applyPeriodicInterest(): void",
                   "+ generateAuditSummary(): String",
                   "+ getAvailableFunds(): double"
               ], border_color="#EC4899")

    # Connect Subclasses to Account with Inheritance Arrows
    draw.line([240, 490, 240, 450], fill="#F59E0B", width=2)
    draw.line([675, 490, 675, 410], fill="#F59E0B", width=2)
    draw.line([1110, 490, 1110, 450], fill="#F59E0B", width=2)
    draw.line([240, 450, 1110, 450], fill="#F59E0B", width=2)
    draw.polygon([(675, 410), (665, 425), (685, 425)], fill="#F59E0B")

    # Exceptions Box (Bottom)
    draw_class(50, 770, 430, 910, "InsufficientFundsException",
               attrs=["- availableBalance: double", "- attemptedAmount: double"],
               methods=["+ toString(): String [ERR-BANK-001]"], border_color="#EF4444")

    draw_class(480, 770, 870, 910, "OverdraftExceededException",
               attrs=["- requestedOverdraft: double", "- maxOverdraftLimit: double"],
               methods=["+ toString(): String [ERR-BANK-002]"], border_color="#EF4444")

    draw_class(920, 770, 1300, 910, "Transaction & Beneficiary POJOs",
               attrs=["Transaction (txnId, accNo, type, amt, bal)", "Beneficiary (src, dest, name, ifsc)"],
               methods=["+ Relational Entity Mappings"], border_color="#06B6D4")

    img.save("docs/class_diagram.png")
    print("[Diagrams]: Generated docs/class_diagram.png")

# -------------------------------------------------------------
# 3. ENTITY RELATIONSHIP (ER) DIAGRAM
# -------------------------------------------------------------
def make_er_diagram():
    w, h = 1200, 700
    img = Image.new("RGB", (w, h), "#0F172A")
    draw = ImageDraw.Draw(img)

    f_title = get_font(26, bold=True)
    f_sub = get_font(15, bold=False)
    f_h = get_font(16, bold=True)
    f_t = get_font(12, bold=False)

    draw.text((w//2, 35), "SB-TMS: Relational DBMS Entity-Relationship (ER) Schema", font=f_title, fill="#F8FAFC", anchor="mm")
    draw.text((w//2, 70), "SQLite Persistence Architecture | Candidate: Anish Vyapari (Roll No: 25CA1012)", font=f_sub, fill="#94A3B8", anchor="mm")

    def draw_entity(x1, y1, x2, y2, title, cols, color="#3B82F6"):
        draw.rounded_rectangle([x1, y1, x2, y2], radius=8, fill="#1E293B", outline=color, width=2)
        draw.rounded_rectangle([x1, y1, x2, y1+30], radius=8, fill=color)
        draw.text(((x1+x2)//2, y1+15), title, font=f_h, fill="#FFFFFF", anchor="mm")

        cy = y1 + 40
        for col, is_pk, is_fk in cols:
            prefix = "[PK] " if is_pk else ("[FK] " if is_fk else "     ")
            p_color = "#F59E0B" if is_pk else ("#10B981" if is_fk else "#CBD5E1")
            draw.text((x1+10, cy), prefix + col, font=f_t, fill=p_color)
            cy += 18

    # 1. ACCOUNTS (Center)
    draw_entity(450, 130, 750, 430, "ACCOUNTS", [
        ("account_number VARCHAR(30)", True, False),
        ("holder_name VARCHAR(100)", False, False),
        ("email VARCHAR(100)", False, False),
        ("phone VARCHAR(20)", False, False),
        ("account_type VARCHAR(20)", False, False),
        ("balance DECIMAL(15,2)", False, False),
        ("interest_rate DECIMAL(5,2)", False, False),
        ("min_balance DECIMAL(15,2)", False, False),
        ("overdraft_limit DECIMAL(15,2)", False, False),
        ("used_overdraft DECIMAL(15,2)", False, False),
        ("tenure_months INTEGER", False, False),
        ("status VARCHAR(20)", False, False),
        ("created_at TIMESTAMP", False, False)
    ], color="#3B82F6")

    # 2. TRANSACTIONS (Right)
    draw_entity(850, 130, 1150, 360, "TRANSACTIONS", [
        ("txn_id VARCHAR(30)", True, False),
        ("account_number VARCHAR(30)", False, True),
        ("txn_type VARCHAR(30)", False, False),
        ("amount DECIMAL(15,2)", False, False),
        ("balance_after DECIMAL(15,2)", False, False),
        ("reference_note TEXT", False, False),
        ("counterparty_account VARCHAR(30)", False, False),
        ("timestamp TIMESTAMP", False, False)
    ], color="#10B981")

    # 3. BENEFICIARIES (Left)
    draw_entity(50, 130, 350, 330, "BENEFICIARIES", [
        ("id INTEGER AUTOINCREMENT", True, False),
        ("source_account VARCHAR(30)", False, True),
        ("beneficiary_account VARCHAR(30)", False, False),
        ("beneficiary_name VARCHAR(100)", False, False),
        ("bank_ifsc VARCHAR(20)", False, False),
        ("max_limit DECIMAL(15,2)", False, False),
        ("added_at TIMESTAMP", False, False)
    ], color="#8B5CF6")

    # 4. AUDIT_LOGS (Bottom Center)
    draw_entity(450, 480, 750, 670, "AUDIT_LOGS", [
        ("log_id INTEGER AUTOINCREMENT", True, False),
        ("action VARCHAR(50)", False, False),
        ("account_number VARCHAR(30)", False, False),
        ("details TEXT", False, False),
        ("status VARCHAR(20)", False, False),
        ("ip_address VARCHAR(45)", False, False),
        ("timestamp TIMESTAMP", False, False)
    ], color="#EF4444")

    # 5. SYSTEM_METRICS (Bottom Right)
    draw_entity(850, 480, 1150, 610, "SYSTEM_METRICS", [
        ("metric_key VARCHAR(50)", True, False),
        ("metric_value TEXT", False, False),
        ("updated_at TIMESTAMP", False, False)
    ], color="#06B6D4")

    # Relationship connectors (Crow's foot / 1-to-Many)
    # ACCOUNTS (1) -> (M) TRANSACTIONS
    draw.line([750, 220, 850, 220], fill="#10B981", width=3)
    draw.text((760, 200), "1", font=f_h, fill="#F8FAFC")
    draw.text((835, 200), "N", font=f_h, fill="#F8FAFC")

    # ACCOUNTS (1) -> (M) BENEFICIARIES
    draw.line([450, 220, 350, 220], fill="#8B5CF6", width=3)
    draw.text((430, 200), "1", font=f_h, fill="#F8FAFC")
    draw.text((360, 200), "N", font=f_h, fill="#F8FAFC")

    # ACCOUNTS (1) -> (M) AUDIT_LOGS
    draw.line([600, 430, 600, 480], fill="#EF4444", width=3)
    draw.text((610, 435), "1", font=f_h, fill="#F8FAFC")
    draw.text((610, 460), "N", font=f_h, fill="#F8FAFC")

    img.save("docs/er_diagram.png")
    print("[Diagrams]: Generated docs/er_diagram.png")

# -------------------------------------------------------------
# 4. SEQUENCE DIAGRAM (Wire Transfer)
# -------------------------------------------------------------
def make_sequence_diagram():
    w, h = 1200, 750
    img = Image.new("RGB", (w, h), "#0F172A")
    draw = ImageDraw.Draw(img)

    f_title = get_font(26, bold=True)
    f_sub = get_font(15, bold=False)
    f_h = get_font(15, bold=True)
    f_t = get_font(12, bold=False)

    draw.text((w//2, 35), "SB-TMS: Atomic Inter-Account Wire Transfer Sequence", font=f_title, fill="#F8FAFC", anchor="mm")
    draw.text((w//2, 70), "ACID Relational Commit & Polymorphic Execution | Candidate: Anish Vyapari (25CA1012)", font=f_sub, fill="#94A3B8", anchor="mm")

    actors = [
        ("Client / UI", 120),
        ("HttpServerApp", 340),
        ("BankingService", 580),
        ("Account (Sender/Receiver)", 840),
        ("Relational DBMS (JDBC)", 1080)
    ]

    for name, x in actors:
        draw.rounded_rectangle([x-80, 110, x+80, 145], radius=6, fill="#1E293B", outline="#3B82F6", width=2)
        draw.text((x, 127), name, font=f_h, fill="#FFFFFF", anchor="mm")
        draw.line([x, 145, x, 680], fill="#475569", width=1)

    steps = [
        (170, 0, 1, "POST /api/transactions/transfer {source, dest, amt}", "#60A5FA"),
        (215, 1, 2, "transfer(src, dest, amount, memo)", "#34D399"),
        (260, 2, 4, "conn.setAutoCommit(false) [Begin ACID Tx]", "#FBBF24"),
        (310, 2, 3, "sender.withdraw(amount) [Domain Boundary Check]", "#F87171"),
        (360, 3, 2, "Withdrawal Confirmed (or throw InsufficientFundsEx)", "#A78BFA"),
        (410, 2, 3, "recipient.deposit(amount, memo)", "#34D399"),
        (460, 2, 4, "UPDATE ACCOUNTS (Sender & Recipient Balances)", "#38BDF8"),
        (510, 2, 4, "INSERT INTO TRANSACTIONS (TRANSFER-OUT & IN)", "#38BDF8"),
        (560, 2, 4, "conn.commit() [ACID Durability Ensured]", "#FBBF24"),
        (610, 2, 1, "Return Transfer Result Map", "#34D399"),
        (650, 1, 0, "HTTP 200 OK {success: true, newBalances...}", "#60A5FA")
    ]

    for y, a1, a2, msg, color in steps:
        x1 = actors[a1][1]
        x2 = actors[a2][1]
        draw.line([x1, y, x2, y], fill=color, width=2)
        # Arrowhead
        dir_mult = 1 if x2 > x1 else -1
        draw.polygon([(x2, y), (x2 - (10*dir_mult), y-5), (x2 - (10*dir_mult), y+5)], fill=color)
        draw.text(((x1+x2)//2, y-10), msg, font=f_t, fill=color, anchor="mm")

    img.save("docs/sequence_diagram.png")
    print("[Diagrams]: Generated docs/sequence_diagram.png")

# -------------------------------------------------------------
# 5. TRANSACTION FLOWCHART
# -------------------------------------------------------------
def make_flowchart():
    w, h = 1000, 750
    img = Image.new("RGB", (w, h), "#0F172A")
    draw = ImageDraw.Draw(img)

    f_title = get_font(26, bold=True)
    f_sub = get_font(15, bold=False)
    f_h = get_font(15, bold=True)
    f_t = get_font(12, bold=False)

    draw.text((w//2, 35), "SB-TMS: Transaction Logic & Domain Boundary Flowchart", font=f_title, fill="#F8FAFC", anchor="mm")
    draw.text((w//2, 70), "Validation, Polymorphism & Exception Trapping | Candidate: Anish Vyapari (25CA1012)", font=f_sub, fill="#94A3B8", anchor="mm")

    def draw_box(cx, cy, bw, bh, text, color="#3B82F6", shape="rect"):
        x1, y1 = cx - bw//2, cy - bh//2
        x2, y2 = cx + bw//2, cy + bh//2
        if shape == "rect":
            draw.rounded_rectangle([x1, y1, x2, y2], radius=8, fill="#1E293B", outline=color, width=2)
        elif shape == "oval":
            draw.ellipse([x1, y1, x2, y2], fill="#1E293B", outline=color, width=2)
        elif shape == "diamond":
            draw.polygon([(cx, y1), (x2, cy), (cx, y2), (x1, cy)], fill="#1E293B", outline=color, width=2)
        draw.text((cx, cy), text, font=f_t, fill="#FFFFFF", anchor="mm", align="center")

    # Flowchart Nodes
    draw_box(w//2, 120, 220, 45, "Client Initiates Transaction\n(Deposit / Withdraw / Transfer)", color="#3B82F6", shape="oval")
    draw.line([w//2, 145, w//2, 185], fill="#64748B", width=2)

    draw_box(w//2, 210, 280, 50, "Is Amount > 0 and\nAccount Numbers Valid?", color="#F59E0B", shape="diamond")

    # Invalid branch (left)
    draw.line([w//2 - 140, 210, 200, 210], fill="#EF4444", width=2)
    draw.line([200, 210, 200, 270], fill="#EF4444", width=2)
    draw.text((260, 195), "No", font=f_t, fill="#EF4444")
    draw_box(200, 300, 240, 55, "Throw InvalidTransactionException\n[ERR-BANK-004] (HTTP 400)", color="#EF4444")

    # Valid branch (down)
    draw.line([w//2, 235, w//2, 285], fill="#10B981", width=2)
    draw.text((w//2 + 25, 255), "Yes", font=f_t, fill="#10B981")

    draw_box(w//2, 310, 260, 45, "Identify Account Type via Polymorphism\n(Savings, Current, or Fixed Deposit)", color="#3B82F6")
    draw.line([w//2, 335, w//2, 375], fill="#64748B", width=2)

    # Decision on Subclass Withdrawal Rules
    draw_box(w//2, 410, 320, 60, "Evaluate Domain Invariants:\nSavings: Bal - Amt >= Rs. 2,000?\nCurrent: Deficit <= OD Limit (50k)?", color="#F59E0B", shape="diamond")

    # Rule violated (right)
    draw.line([w//2 + 160, 410, 800, 410], fill="#EF4444", width=2)
    draw.line([800, 410, 800, 470], fill="#EF4444", width=2)
    draw.text((740, 395), "Rule Violated", font=f_t, fill="#EF4444")
    draw_box(800, 500, 260, 60, "Throw Custom Domain Exception:\nInsufficientFundsException (ERR-001)\nor OverdraftExceededException (ERR-002)", color="#EF4444")

    # Rule satisfied (down)
    draw.line([w//2, 440, w//2, 490], fill="#10B981", width=2)
    draw.text((w//2 + 25, 460), "Compliant", font=f_t, fill="#10B981")

    draw_box(w//2, 520, 280, 55, "Execute State Mutation:\nUpdate Balance & Overdraft Ledger\nAppend 2D Array Transaction Entry", color="#10B981")
    draw.line([w//2, 550, w//2, 595], fill="#64748B", width=2)

    draw_box(w//2, 625, 300, 55, "Execute Relational SQL Persistence:\nINSERT INTO TRANSACTIONS\nUPDATE ACCOUNTS; Record AUDIT_LOG", color="#06B6D4")
    draw.line([w//2, 655, w//2, 695], fill="#64748B", width=2)

    draw_box(w//2, 715, 200, 40, "Return HTTP 200 OK to Client", color="#10B981", shape="oval")

    img.save("docs/transaction_flowchart.png")
    print("[Diagrams]: Generated docs/transaction_flowchart.png")

if __name__ == "__main__":
    make_architecture_diagram()
    make_class_diagram()
    make_er_diagram()
    make_sequence_diagram()
    make_flowchart()
    print("[Diagrams]: All 5 diagrams generated successfully in project/docs/!")
