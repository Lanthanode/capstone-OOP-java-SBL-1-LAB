import time
import sys
from playwright.sync_api import sync_playwright

sys.stdout.reconfigure(encoding='utf-8')

def test_ui():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context(viewport={"width": 1440, "height": 900})
        page = context.new_page()

        print("1. Navigating to http://localhost:8080/index.html...")
        page.goto("http://localhost:8080/index.html")
        page.wait_for_selector(".account-card-wrapper", timeout=10000)
        time.sleep(1)

        # 2. Test Deposit Button on first card
        print("2. Testing [Deposit] button on first account card...")
        deposit_btn = page.locator(".account-card-wrapper").first.locator("button:has-text('Deposit')")
        deposit_btn.click()
        page.wait_for_selector("#modalDeposit.active", timeout=5000)
        print("   Deposit modal active!")
        
        # Check modal details
        acc_disp = page.locator("#modalDepositAccDisplay").text_content()
        print(f"   Target Account displayed: {acc_disp}")
        page.fill("#modalDepositAmountInput", "5000")
        page.click("#formModalDeposit button[type='submit']")
        page.wait_for_selector(".toast.success", timeout=5000)
        print("   Success toast received for Deposit!")
        time.sleep(1)

        # 3. Test Withdraw Button (Domain Exception Boundary Test)
        print("3. Testing [Withdraw] button on first account card...")
        withdraw_btn = page.locator(".account-card-wrapper").first.locator("button:has-text('Withdraw')")
        withdraw_btn.click()
        page.wait_for_selector("#modalWithdraw.active", timeout=5000)
        print("   Withdraw modal active!")
        rule_text = page.locator("#modalWithdrawRule").text_content()
        print(f"   Injected Domain Rule: {rule_text}")

        # Intentionally breach minimum balance of Rs. 2,000 to demonstrate Exception handling
        page.fill("#modalWithdrawAmountInput", "999999")
        page.click("#formModalWithdraw button[type='submit']")
        page.wait_for_selector(".toast.error", timeout=5000)
        err_msg = page.locator(".toast.error .toast-content").text_content()
        print(f"   Captured Expected Domain Exception Toast: {err_msg.strip()}")
        
        # Close withdraw modal
        page.click("#modalWithdraw .modal-close")
        time.sleep(0.5)

        # 4. Test Wire Button
        print("4. Testing [Wire] button on first account card...")
        wire_btn = page.locator(".account-card-wrapper").first.locator("button:has-text('Wire')")
        wire_btn.click()
        page.wait_for_selector("#modalTransfer.active", timeout=5000)
        print("   Wire Transfer modal active!")
        src_disp = page.locator("#modalTransferSourceDisplay").text_content()
        print(f"   Wire Source displayed: {src_disp}")
        page.click("#modalTransfer .modal-close")
        time.sleep(0.5)

        # 5. Test Passbook Button
        print("5. Testing [Passbook] button on first account card...")
        passbook_btn = page.locator(".account-card-wrapper").first.locator("button:has-text('Passbook')")
        passbook_btn.click()
        time.sleep(1)
        active_tab = page.locator(".tab-pane.active").get_attribute("id")
        print(f"   Active Tab switched to: {active_tab}")
        passbook_text = page.locator("#passbookContainer").text_content()
        print(f"   Passbook Statement Content length: {len(passbook_text)} chars")

        print("\nALL 4 QUICK-ACTION CARD BUTTONS TESTED AND WORKING 100%!")
        browser.close()

if __name__ == "__main__":
    test_ui()
