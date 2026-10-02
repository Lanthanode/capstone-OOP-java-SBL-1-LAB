import os
from playwright.sync_api import sync_playwright

def compile_pdf():
    html_path = os.path.abspath("docs/technical_report.html")
    pdf_out1 = os.path.abspath("docs/Capstone_Project_Documentation.pdf")
    pdf_out2 = os.path.abspath("../Capstone_Project_Documentation.pdf")

    print(f"[PDF Compiler]: Loading HTML from {html_path} ...")
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        page = browser.new_page()
        page.goto(f"file:///{html_path.replace(os.sep, '/')}")
        page.wait_for_load_state("networkidle")

        print(f"[PDF Compiler]: Compiling to PDF at {pdf_out1} ...")
        page.pdf(
            path=pdf_out1,
            format="A4",
            print_background=True,
            margin={"top": "14mm", "bottom": "14mm", "left": "14mm", "right": "14mm"},
            display_header_footer=True,
            header_template='<div style="font-size:7pt; color:#64748B; width:100%; text-align:right; padding-right:14mm;">SB-TMS Capstone Technical Specification | RAIT Nerul</div>',
            footer_template='<div style="font-size:7pt; color:#64748B; width:100%; text-align:center;">Candidate: Anish Vyapari (Roll No: 25CA1012 | PRN: DY25ENGU0AIM012 | Batch: A/A1) — Page <span class="pageNumber"></span> of <span class="totalPages"></span></div>'
        )

        # Copy to parent workspace root as well
        if os.path.exists(os.path.dirname(pdf_out2)):
            import shutil
            shutil.copyfile(pdf_out1, pdf_out2)
            print(f"[PDF Compiler]: Also saved PDF copy to {pdf_out2}")

        browser.close()
        print("[PDF Compiler]: PDF compilation completed successfully!")

if __name__ == "__main__":
    compile_pdf()
