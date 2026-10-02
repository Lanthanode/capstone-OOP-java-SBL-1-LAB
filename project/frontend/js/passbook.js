/**
 * Passbook module for SB-TMS
 * Renders the 2D Array Ledger into an official bank passbook statement
 */

let currentPassbookData = null;

document.addEventListener('DOMContentLoaded', () => {
  const sel = document.getElementById('passbookAccSelect');
  if (sel) {
    sel.addEventListener('change', () => {
      loadPassbook(sel.value);
    });
  }

  const printBtn = document.getElementById('btnPrintPassbook');
  if (printBtn) {
    printBtn.addEventListener('click', () => {
      window.print();
    });
  }

  const csvBtn = document.getElementById('btnExportPassbookCsv');
  if (csvBtn) {
    csvBtn.addEventListener('click', exportPassbookCsv);
  }

  const filterSel = document.getElementById('passbookFilterType');
  if (filterSel) {
    filterSel.addEventListener('change', () => {
      if (currentPassbookData) {
        renderPassbookLedger(currentPassbookData.ledger, filterSel.value);
      }
    });
  }
});

async function loadPassbook(accNo) {
  if (!accNo) return;
  const container = document.getElementById('passbookContainer');
  if (!container) return;

  container.innerHTML = `<div style="text-align:center; padding:2rem;">Loading Official Passbook Ledger from DBMS...</div>`;

  try {
    const res = await fetch(`/api/ledger/${accNo}`);
    if (res.ok) {
      currentPassbookData = await res.json();
      renderPassbook(currentPassbookData);
    } else {
      container.innerHTML = `<div style="color:var(--accent-rose); padding:1rem;">Failed to retrieve passbook statement for ${accNo}.</div>`;
    }
  } catch (e) {
    container.innerHTML = `<div style="color:var(--accent-rose); padding:1rem;">Network Error: ${e.message}</div>`;
  }
}

function renderPassbook(data) {
  const container = document.getElementById('passbookContainer');
  if (!container) return;

  container.innerHTML = `
    <div class="passbook-booklet">
      <div class="passbook-header">
        <div class="passbook-title">
          <h2>SMART BANKING & TRANSACTION SYSTEM</h2>
          <p>Official Savings & Current Passbook Ledger | RAIT Nerul Branch (IFSC: SBTMS00025CA)</p>
          <p style="font-size:0.75rem; margin-top:0.25rem;">Candidate: Anish Vyapari (Roll No: 25CA1012 | PRN: DY25ENGU0AIM012 | Batch: A/A1)</p>
        </div>
        <div class="stamp-seal">
          <span>RAIT SBL</span>
          <span>BANK AUDIT</span>
          <span>OFFICIAL</span>
        </div>
      </div>

      <div class="passbook-details-grid">
        <div class="pb-meta-item">
          <span class="pb-label">Account Number</span>
          <span class="pb-value" style="font-family:monospace; color:var(--accent-blue);">${data.accountNumber}</span>
        </div>
        <div class="pb-meta-item">
          <span class="pb-label">Account Holder</span>
          <span class="pb-value">${escapeHtml(data.holderName)}</span>
        </div>
        <div class="pb-meta-item">
          <span class="pb-label">Account Type</span>
          <span class="pb-value">${data.accountType}</span>
        </div>
        <div class="pb-meta-item">
          <span class="pb-label">Account Status</span>
          <span class="pb-value" style="color:var(--accent-emerald);">${data.status}</span>
        </div>
        <div class="pb-meta-item">
          <span class="pb-label">Net Balance</span>
          <span class="pb-value" style="font-size:1.1rem; color:var(--accent-emerald);">${formatCurrency(data.balance)}</span>
        </div>
        <div class="pb-meta-item">
          <span class="pb-label">Available Liquid Funds</span>
          <span class="pb-value">${formatCurrency(data.availableFunds)}</span>
        </div>
      </div>

      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:0.75rem;" class="no-print">
        <h4 style="font-size:0.85rem; text-transform:uppercase; letter-spacing:0.04em;">2D Array Ledger Entries (${data.totalTransactions} Records)</h4>
      </div>

      <div id="passbookTableWrapper">
        <!-- Rendered by renderPassbookLedger -->
      </div>

      <div class="passbook-footer-summary">
        <div>Closing Net Balance: <span style="color:var(--accent-emerald);">${formatCurrency(data.balance)}</span></div>
      </div>
    </div>
  `;

  renderPassbookLedger(data.ledger, 'ALL');
}

function renderPassbookLedger(rows, filter) {
  const wrapper = document.getElementById('passbookTableWrapper');
  if (!wrapper) return;

  let filtered = rows;
  if (filter && filter !== 'ALL') {
    filtered = rows.filter(r => r.type.includes(filter));
  }

  if (filtered.length === 0) {
    wrapper.innerHTML = `<div style="text-align:center; padding:1.5rem; color:#6B7280;">No transactions match filter "${filter}".</div>`;
    return;
  }

  wrapper.innerHTML = `
    <table class="passbook-table">
      <thead>
        <tr>
          <th>TXN ID</th>
          <th>DATE & TIME</th>
          <th>TYPE</th>
          <th>MEMO / PARTICULARS</th>
          <th style="text-align:right;">AMOUNT (Rs.)</th>
          <th style="text-align:right;">BALANCE (Rs.)</th>
        </tr>
      </thead>
      <tbody>
        ${filtered.map(r => `
          <tr>
            <td style="font-weight:700; font-family:monospace;">${r.txnId}</td>
            <td><small>${r.timestamp}</small></td>
            <td><span class="badge-tag ${r.type.toLowerCase()}">${r.type}</span></td>
            <td>${escapeHtml(r.referenceNote || '-')}</td>
            <td style="text-align:right; font-weight:700; color:${getTxnColor(r.type)};">
              ${r.type.includes('WDL') || r.type === 'TRANSFER-OUT' ? '-' : '+'}${r.amount.toFixed(2)}
            </td>
            <td style="text-align:right; font-weight:700;">${r.balanceAfter.toFixed(2)}</td>
          </tr>
        `).join('')}
      </tbody>
    </table>
  `;
}

function getTxnColor(type) {
  if (type.includes('DEP') || type === 'TRANSFER-IN' || type.includes('INT') || type === 'OPEN') return 'var(--accent-emerald)';
  return 'var(--accent-rose)';
}

function exportPassbookCsv() {
  if (!currentPassbookData || !currentPassbookData.ledger) {
    showToast('Export Notice', 'Please select an account with ledger records first.', 'warning');
    return;
  }

  const rows = [
    ['TXN ID', 'TIMESTAMP', 'TYPE', 'AMOUNT', 'BALANCE_AFTER', 'REFERENCE_NOTE', 'COUNTERPARTY']
  ];

  currentPassbookData.ledger.forEach(r => {
    rows.push([
      r.txnId,
      r.timestamp,
      r.type,
      r.amount.toFixed(2),
      r.balanceAfter.toFixed(2),
      `"${(r.referenceNote || '').replace(/"/g, '""')}"`,
      r.counterparty || ''
    ]);
  });

  const csvContent = 'data:text/csv;charset=utf-8,' + rows.map(e => e.join(',')).join('\n');
  const encodedUri = encodeURI(csvContent);
  const link = document.createElement('a');
  link.setAttribute('href', encodedUri);
  link.setAttribute('download', `Passbook_${currentPassbookData.accountNumber}.csv`);
  document.body.appendChild(link);
  link.click();
  link.remove();
  showToast('Export Success', `Downloaded passbook statement CSV for ${currentPassbookData.accountNumber}`, 'success');
}

window.loadPassbook = loadPassbook;
