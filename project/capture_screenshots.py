import os
import time
from playwright.sync_api import sync_playwright

def capture_all():
    os.makedirs("screenshots", exist_ok=True)
    
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context(viewport={"width": 1400, "height": 900})
        page = context.new_page()

        print("[Playwright]: Navigating to http://localhost:8080/index.html ...")
        page.goto("http://localhost:8080/index.html")
        page.wait_for_selector(".bank-card", timeout=10000)
        time.sleep(1)

        # 1. Dashboard Overview
        print("[Playwright]: Capturing 01_dashboard_overview.png ...")
        page.screenshot(path="screenshots/01_dashboard_overview.png")

        # 2. Transaction Terminal
        print("[Playwright]: Capturing 02_transaction_terminal.png ...")
        page.click("button[data-tab='transactions']")
        time.sleep(1)
        page.screenshot(path="screenshots/02_transaction_terminal.png")

        # 3. Official Passbook
        print("[Playwright]: Capturing 03_passbook_statement.png ...")
        page.click("button[data-tab='passbook']")
        time.sleep(1)
        page.screenshot(path="screenshots/03_passbook_statement.png")

        # 4. Quarter-End Servicing
        print("[Playwright]: Capturing 04_quarter_end_polymorphism.png ...")
        page.click("button[data-tab='quarter-end']")
        time.sleep(1)
        # Click the batch run button
        page.click("#btnQuarterEndBatch")
        time.sleep(1.5)
        page.screenshot(path="screenshots/04_quarter_end_polymorphism.png")

        # 5. SQL DBMS Inspector
        print("[Playwright]: Capturing 05_sql_dbms_inspector.png ...")
        page.click("button[data-tab='sql-inspector']")
        time.sleep(1)
        page.click("#btnRunSql")
        time.sleep(1)
        page.screenshot(path="screenshots/05_sql_dbms_inspector.png")

        # 6. Audit Trail & Security
        print("[Playwright]: Capturing 06_audit_trail_security.png ...")
        page.click("button[data-tab='audit-logs']")
        time.sleep(1)
        page.screenshot(path="screenshots/06_audit_trail_security.png")

        # 7. Testing Domain Exception Toast
        print("[Playwright]: Capturing 07_domain_exception_toast.png ...")
        page.click("button[data-tab='transactions']")
        time.sleep(0.5)
        page.fill("#withdrawAmountInput", "999999")
        page.click("#formWithdraw button[type='submit']")
        time.sleep(1)
        page.screenshot(path="screenshots/07_domain_exception_toast.png")

        browser.close()
        print("[Playwright]: All 7 live screenshots successfully captured and saved to project/screenshots/!")

if __name__ == "__main__":
    capture_all()
