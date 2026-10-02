/**
 * SB-TMS Capstone Application Frontend Controller
 * Candidate: Anish Vyapari (Roll No: 25CA1012 | PRN: DY25ENGU0AIM012 | Batch: A/A1)
 * Institution: Ramrao Adik Institute of Technology, Nerul
 */

const API_BASE = '/api';

const state = {
  accounts: [],
  metrics: null,
  activeAccount: null,
  selectedTab: 'dashboard',
  theme: 'dark'
};

// --- INITIALIZATION ---
document.addEventListener('DOMContentLoaded', () => {
  initTheme();
  setupNavigation();
  loadAllData();
  setupEventListeners();

  // Auto-refresh metrics every 30 seconds
  setInterval(loadAllData, 30000);
});

function initTheme() {
  const saved = localStorage.getItem('sbtms_theme') || 'dark';
  state.theme = saved;
  document.documentElement.setAttribute('data-theme', saved);
  const themeBtn = document.getElementById('themeToggleBtn');
  if (themeBtn) {
    themeBtn.innerHTML = saved === 'dark' ? '☀️' : '🌙';
  }
}

function toggleTheme() {
  const newTheme = state.theme === 'dark' ? 'light' : 'dark';
  state.theme = newTheme;
  document.documentElement.setAttribute('data-theme', newTheme);
  localStorage.setItem('sbtms_theme', newTheme);
  const themeBtn = document.getElementById('themeToggleBtn');
  if (themeBtn) {
    themeBtn.innerHTML = newTheme === 'dark' ? '☀️' : '🌙';
  }
}

function setupNavigation() {
  const tabs = document.querySelectorAll('.tab-btn');
  tabs.forEach(tab => {
    tab.addEventListener('click', () => {
      const target = tab.getAttribute('data-tab');
      switchTab(target);
    });
  });

  // Handle hash changes
  if (window.location.hash) {
    const hashTab = window.location.hash.replace('#', '');
    if (document.getElementById(`tab-${hashTab}`)) {
      switchTab(hashTab);
    }
  }
}

function switchTab(tabId) {
  state.selectedTab = tabId;
  window.location.hash = tabId;

  document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.classList.toggle('active', btn.getAttribute('data-tab') === tabId);
  });

  document.querySelectorAll('.tab-pane').forEach(pane => {
    pane.classList.toggle('active', pane.id === `tab-${tabId}`);
  });

  if (tabId === 'audit-logs') {
    loadAuditLogs();
  } else if (tabId === 'passbook' && state.activeAccount) {
    window.loadPassbook(state.activeAccount.accountNumber);
  }
}

async function loadAllData() {
  try {
    const [accRes, metricRes] = await Promise.all([
      fetch(`${API_BASE}/accounts`),
      fetch(`${API_BASE}/analytics`)
    ]);

    if (accRes.ok) {
      state.accounts = await accRes.json();
      renderAccountsGrid();
      populateAccountDropdowns();
    }

    if (metricRes.ok) {
      state.metrics = await metricRes.json();
      renderHeroMetrics();
    }
  } catch (err) {
    console.error('Error fetching banking data:', err);
    showToast('Connection Warning', 'Unable to reach Java backend REST API at ' + API_BASE, 'warning');
  }
}

function renderHeroMetrics() {
  if (!state.metrics) return;
  const m = state.metrics;

  const totalEl = document.getElementById('metric-total-deposits');
  if (totalEl) totalEl.textContent = formatCurrency(m.totalPortfolioValue || 0);

  const netEl = document.getElementById('metric-net-asset');
  if (netEl) netEl.textContent = formatCurrency(m.netAssetValue || 0);

  const odEl = document.getElementById('metric-overdraft-usage');
  if (odEl) {
    odEl.textContent = formatCurrency(m.activeOverdraftLiability || 0);
    const sub = document.getElementById('metric-overdraft-capacity');
    if (sub) sub.textContent = `Limit: ${formatCurrency(m.approvedOverdraftCapacity || 0)}`;
  }

  const liqEl = document.getElementById('metric-liquidity-ratio');
  if (liqEl) liqEl.textContent = `${m.liquidityRatio || 100}%`;

  const totalAccBadge = document.getElementById('badge-total-accounts');
  if (totalAccBadge) totalAccBadge.textContent = `${m.totalAccounts || 0} Accounts`;

  const totalTxnBadge = document.getElementById('badge-total-transactions');
  if (totalTxnBadge) totalTxnBadge.textContent = `${m.totalTransactionCount || 0} Txns`;
}

function renderAccountsGrid() {
  const container = document.getElementById('accountsGrid');
  if (!container) return;

  if (state.accounts.length === 0) {
    container.innerHTML = `<div class="empty-state">No accounts provisioned. Click "New Account" to begin.</div>`;
    return;
  }

  container.innerHTML = state.accounts.map(acc => {
    const typeClass = acc.accountType.toLowerCase().replace('_', '-');
    const isCurrent = acc.accountType === 'CURRENT';
    const isSavings = acc.accountType === 'SAVINGS';
    const isFD = acc.accountType === 'FIXED_DEPOSIT';

    let specificDetail = '';
    if (isSavings) {
      specificDetail = `
        <div class="detail-row"><span class="k">Interest Rate:</span><span class="v" style="color:var(--accent-emerald);">4.25% p.a.</span></div>
        <div class="detail-row"><span class="k">Min Balance:</span><span class="v">Rs. 2,000.00</span></div>
      `;
    } else if (isCurrent) {
      specificDetail = `
        <div class="detail-row"><span class="k">Overdraft Limit:</span><span class="v">Rs. 50,000.00</span></div>
        <div class="detail-row"><span class="k">Active OD Drawn:</span><span class="v" style="color:${acc.usedOverdraft > 0 ? 'var(--accent-rose)' : 'inherit'};">Rs. ${(acc.usedOverdraft || 0).toFixed(2)}</span></div>
      `;
    } else if (isFD) {
      specificDetail = `
        <div class="detail-row"><span class="k">Interest Rate:</span><span class="v" style="color:var(--accent-cyan);">7.50% p.a.</span></div>
        <div class="detail-row"><span class="k">Tenure:</span><span class="v">12 Months</span></div>
      `;
    }

    return `
      <div class="account-card-wrapper" data-acc="${acc.accountNumber}">
        <div class="bank-card ${typeClass}" onclick="selectAccountForAction('${acc.accountNumber}')">
          <div class="bank-card-top">
            <img src="assets/chip.svg" class="bank-card-chip" alt="Chip">
            <span class="bank-card-type">${acc.accountType}</span>
          </div>
          <div class="bank-card-balance">
            <div class="lbl">Net Available Balance</div>
            <div class="amt">${formatCurrency(acc.balance)}</div>
          </div>
          <div class="bank-card-bottom">
            <div class="card-holder">${escapeHtml(acc.holderName)}</div>
            <div class="card-number">${acc.accountNumber}</div>
          </div>
        </div>
        <div class="account-detail-box">
          <div class="detail-row"><span class="k">Email:</span><span class="v">${escapeHtml(acc.email || 'N/A')}</span></div>
          <div class="detail-row"><span class="k">Available Funds:</span><span class="v" style="color:var(--accent-emerald); font-weight:700;">${formatCurrency(acc.availableFunds)}</span></div>
          ${specificDetail}
          <div class="card-actions-strip">
            <button class="btn-card-action" onclick="openDepositModal('${acc.accountNumber}')">Deposit</button>
            <button class="btn-card-action" onclick="openWithdrawModal('${acc.accountNumber}')">Withdraw</button>
            <button class="btn-card-action" onclick="openTransferModal('${acc.accountNumber}')">Wire</button>
            <button class="btn-card-action" onclick="viewPassbookForAccount('${acc.accountNumber}')">Passbook</button>
          </div>
        </div>
      </div>
    `;
  }).join('');
}

function populateAccountDropdowns() {
  const selects = ['depositAccSelect', 'withdrawAccSelect', 'transferSourceSelect', 'transferDestSelect', 'passbookAccSelect'];
  selects.forEach(id => {
    const el = document.getElementById(id);
    if (!el) return;
    const currentVal = el.value;
    el.innerHTML = state.accounts.map(acc => 
      `<option value="${acc.accountNumber}">${acc.accountNumber} - ${acc.holderName} (${acc.accountType} | Bal: Rs. ${acc.balance.toFixed(2)})</option>`
    ).join('');
    if (currentVal) el.value = currentVal;
  });
}

function selectAccountForAction(accNo) {
  const found = state.accounts.find(a => a.accountNumber === accNo);
  if (found) {
    state.activeAccount = found;
    showToast('Account Selected', `${found.holderName} (${found.accountNumber})`, 'info');
  }
}

function viewPassbookForAccount(accNo) {
  const found = state.accounts.find(a => a.accountNumber === accNo);
  if (found) state.activeAccount = found;
  switchTab('passbook');
  const sel = document.getElementById('passbookAccSelect');
  if (sel) sel.value = accNo;
  window.loadPassbook(accNo);
}

// --- MODAL UTILITIES ---
function openModal(modalId) {
  const m = document.getElementById(modalId);
  if (m) m.classList.add('active');
}

function closeModal(modalId) {
  const m = document.getElementById(modalId);
  if (m) m.classList.remove('active');
}

function openDepositModal(accNo) {
  const acc = state.accounts.find(a => a.accountNumber === accNo);
  if (!acc) return;
  state.activeAccount = acc;

  const inputEl = document.getElementById('modalDepositAccInput');
  if (inputEl) inputEl.value = acc.accountNumber;
  const dispEl = document.getElementById('modalDepositAccDisplay');
  if (dispEl) dispEl.textContent = `${acc.accountNumber} (${acc.accountType})`;
  const nameEl = document.getElementById('modalDepositHolderName');
  if (nameEl) nameEl.textContent = acc.holderName;
  const balEl = document.getElementById('modalDepositCurrentBal');
  if (balEl) balEl.textContent = formatCurrency(acc.balance);
  const amtInput = document.getElementById('modalDepositAmountInput');
  if (amtInput) amtInput.value = '';
  const noteInput = document.getElementById('modalDepositNoteInput');
  if (noteInput) noteInput.value = 'Cash Deposit';

  // Also sync the transaction terminal dropdown
  const sel = document.getElementById('depositAccSelect');
  if (sel) sel.value = accNo;

  openModal('modalDeposit');
  setTimeout(() => {
    const inp = document.getElementById('modalDepositAmountInput');
    if (inp) inp.focus();
  }, 120);
}

function openWithdrawModal(accNo) {
  const acc = state.accounts.find(a => a.accountNumber === accNo);
  if (!acc) return;
  state.activeAccount = acc;

  const inputEl = document.getElementById('modalWithdrawAccInput');
  if (inputEl) inputEl.value = acc.accountNumber;
  const dispEl = document.getElementById('modalWithdrawAccDisplay');
  if (dispEl) dispEl.textContent = `${acc.accountNumber} (${acc.accountType})`;
  const nameEl = document.getElementById('modalWithdrawHolderName');
  if (nameEl) nameEl.textContent = acc.holderName;
  const balEl = document.getElementById('modalWithdrawCurrentBal');
  if (balEl) balEl.textContent = formatCurrency(acc.availableFunds !== undefined ? acc.availableFunds : acc.balance);
  const amtInput = document.getElementById('modalWithdrawAmountInput');
  if (amtInput) amtInput.value = '';
  const noteInput = document.getElementById('modalWithdrawNoteInput');
  if (noteInput) noteInput.value = 'ATM Cash Withdrawal';

  const ruleEl = document.getElementById('modalWithdrawRule');
  if (ruleEl) {
    if (acc.accountType === 'SAVINGS') {
      ruleEl.innerHTML = '<span style="color:var(--accent-amber); font-weight:600;">⚠️ Statutory Reserve Rule:</span> Must maintain min balance <strong>Rs. 2,000.00</strong> (Breaches trigger <code>InsufficientFundsException ERR-BANK-001</code>)';
    } else if (acc.accountType === 'CURRENT') {
      const availOD = 50000.0 - (acc.usedOverdraft || 0);
      ruleEl.innerHTML = `<span style="color:var(--accent-cyan); font-weight:600;">⚡ Overdraft Credit Facility:</span> Up to <strong>Rs. 50,000.00</strong> approved (Available OD: <strong>Rs. ${availOD.toFixed(2)}</strong> | Breaches trigger <code>OverdraftExceededException ERR-BANK-002</code>)`;
    } else {
      ruleEl.innerHTML = '<span style="color:var(--accent-indigo); font-weight:600;">🔒 Fixed Deposit Term Account:</span> Premature liquidations enforce early penalty deduction.';
    }
  }

  // Also sync the transaction terminal dropdown
  const sel = document.getElementById('withdrawAccSelect');
  if (sel) sel.value = accNo;

  openModal('modalWithdraw');
  setTimeout(() => {
    const inp = document.getElementById('modalWithdrawAmountInput');
    if (inp) inp.focus();
  }, 120);
}

function openTransferModal(accNo) {
  const acc = state.accounts.find(a => a.accountNumber === accNo);
  if (!acc) return;
  state.activeAccount = acc;

  const srcInput = document.getElementById('modalTransferSourceInput');
  if (srcInput) srcInput.value = acc.accountNumber;
  const srcDisp = document.getElementById('modalTransferSourceDisplay');
  if (srcDisp) srcDisp.textContent = `${acc.accountNumber} - ${acc.holderName} (${acc.accountType})`;
  const balEl = document.getElementById('modalTransferCurrentBal');
  if (balEl) balEl.textContent = formatCurrency(acc.availableFunds !== undefined ? acc.availableFunds : acc.balance);
  const amtInput = document.getElementById('modalTransferAmountInput');
  if (amtInput) amtInput.value = '';
  const memoInput = document.getElementById('modalTransferMemoInput');
  if (memoInput) memoInput.value = 'Online Wire Transfer';

  const destSelect = document.getElementById('modalTransferDestSelect');
  if (destSelect) {
    const others = state.accounts.filter(a => a.accountNumber !== accNo);
    destSelect.innerHTML = others.map(a => 
      `<option value="${a.accountNumber}">${a.accountNumber} - ${a.holderName} (${a.accountType} | Bal: Rs. ${a.balance.toFixed(2)})</option>`
    ).join('');
  }

  // Also sync the transaction terminal dropdown
  const sel = document.getElementById('transferSourceSelect');
  if (sel) sel.value = accNo;

  openModal('modalTransfer');
  setTimeout(() => {
    const inp = document.getElementById('modalTransferAmountInput');
    if (inp) inp.focus();
  }, 120);
}

function openNewAccountModal() {
  openModal('modalNewAccount');
}

// --- AUDIT LOGS FETCHER ---
async function loadAuditLogs() {
  const container = document.getElementById('auditLogsTbody');
  if (!container) return;

  try {
    const res = await fetch(`${API_BASE}/audit-logs`);
    if (res.ok) {
      const logs = await res.json();
      if (logs.length === 0) {
        container.innerHTML = `<tr><td colspan="6" style="text-align:center; padding:1.5rem;">No audit logs recorded yet.</td></tr>`;
        return;
      }
      container.innerHTML = logs.map(l => {
        let badgeClass = 'dep';
        if (l.status === 'BLOCKED' || l.status === 'FAILED') badgeClass = 'wdl';
        else if (l.status === 'WARNING') badgeClass = 'int';

        return `
          <tr>
            <td>#${l.logId}</td>
            <td><strong>${l.action}</strong></td>
            <td>${l.accountNumber || 'SYSTEM'}</td>
            <td>${escapeHtml(l.details)}</td>
            <td><span class="badge-tag ${badgeClass}">${l.status}</span></td>
            <td><small>${l.timestamp}</small></td>
          </tr>
        `;
      }).join('');
    }
  } catch (e) {
    console.error('Failed to load audit logs:', e);
  }
}

// --- BATCH QUARTER-END SERVICING RUNNER ---
async function executeQuarterEndBatch() {
  const btn = document.getElementById('btnQuarterEndBatch');
  if (btn) {
    btn.disabled = true;
    btn.innerHTML = 'Executing Runtime Polymorphism...';
  }

  try {
    const res = await fetch(`${API_BASE}/batch/quarter-end`, { method: 'POST' });
    const data = await res.json();

    if (res.ok) {
      showToast('Quarter-End Servicing Completed', 
        `Applied dynamic interest credits & overdraft fee levies across ${data.length} accounts!`, 'success');
      
      const resContainer = document.getElementById('batchResultsBox');
      if (resContainer) {
        resContainer.innerHTML = `
          <div style="background:rgba(16, 185, 129, 0.1); border:1px solid rgba(16, 185, 129, 0.3); border-radius:8px; padding:1rem; margin-top:1rem;">
            <h4 style="color:var(--accent-emerald); margin-bottom:0.5rem;">Dynamic Method Dispatch Executed Successfully</h4>
            <div style="display:flex; flex-direction:column; gap:0.5rem; font-size:0.85rem;">
              ${data.map(item => `
                <div style="display:flex; justify-content:space-between; border-bottom:1px solid rgba(255,255,255,0.05); padding-bottom:0.3rem;">
                  <span><strong>${item.accountNumber}</strong> (${item.accountType}) - ${item.holderName}</span>
                  <span>Rs. ${item.preBalance.toFixed(2)} → <strong style="color:var(--accent-emerald);">Rs. ${item.postBalance.toFixed(2)}</strong></span>
                </div>
              `).join('')}
            </div>
          </div>
        `;
      }
      loadAllData();
    } else {
      showToast('Batch Fault', data.errorMessage || data.error || 'Execution failed', 'error');
    }
  } catch (err) {
    showToast('Batch Error', err.message, 'error');
  } finally {
    if (btn) {
      btn.disabled = false;
      btn.innerHTML = '⚡ Run End-of-Quarter Servicing (Runtime Polymorphism)';
    }
  }
}

// --- RESET DATABASE TO BENCHMARK SEED ---
async function resetBenchmarkDatabase() {
  if (!confirm('Reset banking database to original Experiment 11 benchmark seed data? All custom transactions will be refreshed.')) {
    return;
  }

  try {
    const res = await fetch(`${API_BASE}/system/reset`, { method: 'POST' });
    if (res.ok) {
      showToast('System Reset', 'Restored to Experiment 11 benchmark accounts and ledger.', 'success');
      loadAllData();
      if (window.loadPassbook && state.activeAccount) {
        window.loadPassbook(state.activeAccount.accountNumber);
      }
    }
  } catch (e) {
    showToast('Reset Error', e.message, 'error');
  }
}

// --- TOAST ALERTS ---
function showToast(title, message, type = 'info') {
  const container = document.getElementById('toastContainer');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.innerHTML = `
    <div class="toast-content">
      <h4>${escapeHtml(title)}</h4>
      <p>${escapeHtml(message)}</p>
    </div>
  `;

  container.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(100%)';
    setTimeout(() => toast.remove(), 300);
  }, 4500);
}

// --- FORMATTERS ---
function formatCurrency(val) {
  const num = Number(val) || 0;
  return 'Rs. ' + num.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

function setupEventListeners() {
  // Theme toggle
  const themeBtn = document.getElementById('themeToggleBtn');
  if (themeBtn) themeBtn.addEventListener('click', toggleTheme);

  // Reset DB button
  const resetBtn = document.getElementById('btnResetDb');
  if (resetBtn) resetBtn.addEventListener('click', resetBenchmarkDatabase);

  // Quarter-end batch button
  const batchBtn = document.getElementById('btnQuarterEndBatch');
  if (batchBtn) batchBtn.addEventListener('click', executeQuarterEndBatch);

  // Close modals on backdrop click
  document.querySelectorAll('.modal-backdrop').forEach(b => {
    b.addEventListener('click', (e) => {
      if (e.target === b) b.classList.remove('active');
    });
  });
}

// Export globals
window.showToast = showToast;
window.formatCurrency = formatCurrency;
window.loadAllData = loadAllData;
window.openModal = openModal;
window.closeModal = closeModal;
window.openDepositModal = openDepositModal;
window.openWithdrawModal = openWithdrawModal;
window.openTransferModal = openTransferModal;
window.openNewAccountModal = openNewAccountModal;
window.viewPassbookForAccount = viewPassbookForAccount;
window.switchTab = switchTab;
