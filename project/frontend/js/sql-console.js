/**
 * Interactive SQL & Relational DBMS Console Module for SB-TMS
 * Evaluator tool allowing direct query execution on the SQLite database
 */

document.addEventListener('DOMContentLoaded', () => {
  setupSqlConsole();
});

function setupSqlConsole() {
  const btnRun = document.getElementById('btnRunSql');
  const textarea = document.getElementById('sqlQueryTextarea');

  if (btnRun && textarea) {
    btnRun.addEventListener('click', () => {
      runSqlQuery(textarea.value);
    });

    textarea.addEventListener('keydown', (e) => {
      // Ctrl+Enter or Cmd+Enter to execute
      if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
        e.preventDefault();
        runSqlQuery(textarea.value);
      }
    });
  }

  // Pre-built query chips
  document.querySelectorAll('.chip-sql').forEach(chip => {
    chip.addEventListener('click', () => {
      const q = chip.getAttribute('data-sql');
      if (textarea && q) {
        textarea.value = q;
        runSqlQuery(q);
      }
    });
  });
}

async function runSqlQuery(sql) {
  if (!sql || !sql.trim()) {
    showToast('SQL Notice', 'Please enter a valid SQL statement to execute.', 'warning');
    return;
  }

  const resultsBox = document.getElementById('sqlResultsContainer');
  if (!resultsBox) return;

  resultsBox.innerHTML = `
    <div style="text-align:center; padding:1.5rem; color:var(--text-secondary);">
      <span class="spinner"></span> Executing SQL statement on Relational DBMS...
    </div>
  `;

  try {
    const res = await fetch('/api/sql/query', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ sql: sql.trim() })
    });
    const data = await res.json();

    if (data.success) {
      if (data.type === 'SELECT') {
        renderSqlSelectResults(data);
      } else {
        renderSqlUpdateResults(data);
      }
      showToast('Query Executed', `Finished in ${data.executionTimeMs} ms (${data.rowCount || data.affectedRows || 0} rows)`, 'success');
    } else {
      renderSqlError(data);
      showToast('SQL Execution Error', data.error || 'Failed', 'error');
    }
  } catch (err) {
    resultsBox.innerHTML = `<div style="color:var(--accent-rose); padding:1rem;">Network Error: ${err.message}</div>`;
  }
}

function renderSqlSelectResults(data) {
  const resultsBox = document.getElementById('sqlResultsContainer');
  if (!resultsBox) return;

  const cols = data.columns || [];
  const rows = data.rows || [];

  resultsBox.innerHTML = `
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:0.75rem; font-size:0.78rem; color:var(--text-secondary);">
      <span>Query executed in <strong style="color:var(--accent-emerald);">${data.executionTimeMs} ms</strong></span>
      <span>Total Records: <strong>${data.rowCount}</strong></span>
    </div>
    <div class="table-responsive">
      <table class="data-table">
        <thead>
          <tr>
            ${cols.map(c => `<th>${escapeHtml(c)}</th>`).join('')}
          </tr>
        </thead>
        <tbody>
          ${rows.length === 0 ? `<tr><td colspan="${cols.length}" style="text-align:center;">Empty result set (0 rows returned)</td></tr>` : ''}
          ${rows.map(r => `
            <tr>
              ${r.map(cell => `<td>${escapeHtml(cell !== null && cell !== undefined ? String(cell) : 'NULL')}</td>`).join('')}
            </tr>
          `).join('')}
        </tbody>
      </table>
    </div>
  `;
}

function renderSqlUpdateResults(data) {
  const resultsBox = document.getElementById('sqlResultsContainer');
  if (!resultsBox) return;

  resultsBox.innerHTML = `
    <div style="background:rgba(16, 185, 129, 0.1); border:1px solid rgba(16, 185, 129, 0.3); border-radius:8px; padding:1.25rem;">
      <h4 style="color:var(--accent-emerald); margin-bottom:0.4rem;">Statement Executed Successfully</h4>
      <p style="font-size:0.85rem; color:var(--text-secondary);">${escapeHtml(data.message)}</p>
      <div style="margin-top:0.5rem; font-size:0.78rem; color:var(--text-muted);">
        Execution Time: ${data.executionTimeMs} ms | Affected Rows: ${data.affectedRows}
      </div>
    </div>
  `;
  loadAllData();
}

function renderSqlError(data) {
  const resultsBox = document.getElementById('sqlResultsContainer');
  if (!resultsBox) return;

  resultsBox.innerHTML = `
    <div style="background:rgba(239, 68, 68, 0.1); border:1px solid rgba(239, 68, 68, 0.3); border-radius:8px; padding:1.25rem;">
      <h4 style="color:var(--accent-rose); margin-bottom:0.4rem;">SQL Engine Fault</h4>
      <p style="font-family:monospace; font-size:0.85rem; color:#FCA5A5;">${escapeHtml(data.error)}</p>
      ${data.errorCode ? `<div style="margin-top:0.4rem; font-size:0.75rem; color:var(--text-muted);">SQLite Error Code: ${data.errorCode}</div>` : ''}
    </div>
  `;
}
