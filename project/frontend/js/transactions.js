/**
 * Transaction processing module for SB-TMS
 * Handles deposit, withdrawal, and wire transfer with real domain error validation
 */

document.addEventListener('DOMContentLoaded', () => {
  setupTransactionForms();
});

function setupTransactionForms() {
  // 1. Deposit Form
  const depositForm = document.getElementById('formDeposit');
  if (depositForm) {
    depositForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const accNo = document.getElementById('depositAccSelect').value;
      const amount = parseFloat(document.getElementById('depositAmountInput').value);
      const note = document.getElementById('depositNoteInput').value;

      if (!accNo || isNaN(amount) || amount <= 0) {
        showToast('Validation Error', 'Please enter a valid positive deposit amount.', 'warning');
        return;
      }

      try {
        const res = await fetch('/api/transactions/deposit', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ accountNumber: accNo, amount: amount, referenceNote: note })
        });
        const data = await res.json();

        if (res.ok) {
          showToast('Deposit Successful', `Rs. ${amount.toFixed(2)} deposited into ${accNo}. New Balance: Rs. ${data.balanceAfter.toFixed(2)}`, 'success');
          closeModal('modalDeposit');
          depositForm.reset();
          loadAllData();
        } else {
          showToast('Deposit Failed', data.errorMessage || data.error || 'Server error', 'error');
        }
      } catch (err) {
        showToast('Network Error', err.message, 'error');
      }
    });
  }

  // 2. Withdrawal Form (Domain Exception Enforcement)
  const withdrawForm = document.getElementById('formWithdraw');
  if (withdrawForm) {
    withdrawForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const accNo = document.getElementById('withdrawAccSelect').value;
      const amount = parseFloat(document.getElementById('withdrawAmountInput').value);

      if (!accNo || isNaN(amount) || amount <= 0) {
        showToast('Validation Error', 'Please enter a valid positive withdrawal amount.', 'warning');
        return;
      }

      try {
        const res = await fetch('/api/transactions/withdraw', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ accountNumber: accNo, amount: amount })
        });
        const data = await res.json();

        if (res.ok) {
          showToast('Withdrawal Approved', `Rs. ${amount.toFixed(2)} dispensed from ${accNo}. Balance: Rs. ${data.balanceAfter.toFixed(2)}`, 'success');
          closeModal('modalWithdraw');
          withdrawForm.reset();
          loadAllData();
        } else {
          // Explicitly highlights domain exceptions from Experiment 11 (ERR-BANK-001 or ERR-BANK-002)
          const errCode = data.errorCode || 'EXCEPTION';
          showToast(`Domain Exception [${errCode}]`, data.errorMessage || data.error, 'error');
        }
      } catch (err) {
        showToast('Network Error', err.message, 'error');
      }
    });
  }

  // 3. Inter-Account Wire Transfer Form (ACID Transaction)
  const transferForm = document.getElementById('formTransfer');
  if (transferForm) {
    transferForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const sourceAcc = document.getElementById('transferSourceSelect').value;
      const destAcc = document.getElementById('transferDestSelect').value;
      const amount = parseFloat(document.getElementById('transferAmountInput').value);
      const memo = document.getElementById('transferMemoInput').value;

      if (!sourceAcc || !destAcc || isNaN(amount) || amount <= 0) {
        showToast('Validation Error', 'Please verify source, destination, and amount.', 'warning');
        return;
      }

      if (sourceAcc === destAcc) {
        showToast('Domain Rule Error', 'Self-transfer is prohibited. Source and destination must differ.', 'warning');
        return;
      }

      try {
        const res = await fetch('/api/transactions/transfer', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            sourceAccount: sourceAcc,
            destAccount: destAcc,
            amount: amount,
            referenceNote: memo
          })
        });
        const data = await res.json();

        if (res.ok) {
          showToast('Wire Transfer Executed', 
            `Rs. ${amount.toFixed(2)} transferred from ${sourceAcc} to ${destAcc} with ACID commit.`, 'success');
          closeModal('modalTransfer');
          transferForm.reset();
          loadAllData();
        } else {
          const errCode = data.errorCode || 'ROLLBACK';
          showToast(`Transfer Fault [${errCode}]`, data.errorMessage || data.error, 'error');
        }
      } catch (err) {
        showToast('Network Error', err.message, 'error');
      }
    });
  }

  // 4. Modal Deposit Form
  const modalDepositForm = document.getElementById('formModalDeposit');
  if (modalDepositForm) {
    modalDepositForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const accNo = document.getElementById('modalDepositAccInput').value;
      const amount = parseFloat(document.getElementById('modalDepositAmountInput').value);
      const note = document.getElementById('modalDepositNoteInput').value;

      if (!accNo || isNaN(amount) || amount <= 0) {
        showToast('Validation Error', 'Please enter a valid positive deposit amount.', 'warning');
        return;
      }

      try {
        const res = await fetch('/api/transactions/deposit', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ accountNumber: accNo, amount: amount, referenceNote: note })
        });
        const data = await res.json();

        if (res.ok) {
          showToast('Deposit Successful', `Rs. ${amount.toFixed(2)} deposited into ${accNo}. New Balance: Rs. ${data.balanceAfter.toFixed(2)}`, 'success');
          closeModal('modalDeposit');
          modalDepositForm.reset();
          loadAllData();
        } else {
          showToast('Deposit Failed', data.errorMessage || data.error || 'Server error', 'error');
        }
      } catch (err) {
        showToast('Network Error', err.message, 'error');
      }
    });
  }

  // 5. Modal Withdrawal Form (Domain Exception Enforcement)
  const modalWithdrawForm = document.getElementById('formModalWithdraw');
  if (modalWithdrawForm) {
    modalWithdrawForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const accNo = document.getElementById('modalWithdrawAccInput').value;
      const amount = parseFloat(document.getElementById('modalWithdrawAmountInput').value);
      const note = document.getElementById('modalWithdrawNoteInput').value;

      if (!accNo || isNaN(amount) || amount <= 0) {
        showToast('Validation Error', 'Please enter a valid positive withdrawal amount.', 'warning');
        return;
      }

      try {
        const res = await fetch('/api/transactions/withdraw', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ accountNumber: accNo, amount: amount, referenceNote: note })
        });
        const data = await res.json();

        if (res.ok) {
          showToast('Withdrawal Approved', `Rs. ${amount.toFixed(2)} dispensed from ${accNo}. Balance: Rs. ${data.balanceAfter.toFixed(2)}`, 'success');
          closeModal('modalWithdraw');
          modalWithdrawForm.reset();
          loadAllData();
        } else {
          const errCode = data.errorCode || 'EXCEPTION';
          showToast(`Domain Exception [${errCode}]`, data.errorMessage || data.error, 'error');
        }
      } catch (err) {
        showToast('Network Error', err.message, 'error');
      }
    });
  }

  // 6. Modal Wire Transfer Form (ACID Transaction)
  const modalTransferForm = document.getElementById('formModalTransfer');
  if (modalTransferForm) {
    modalTransferForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const sourceAcc = document.getElementById('modalTransferSourceInput').value;
      const destAcc = document.getElementById('modalTransferDestSelect').value;
      const amount = parseFloat(document.getElementById('modalTransferAmountInput').value);
      const memo = document.getElementById('modalTransferMemoInput').value;

      if (!sourceAcc || !destAcc || isNaN(amount) || amount <= 0) {
        showToast('Validation Error', 'Please verify source, destination, and amount.', 'warning');
        return;
      }

      if (sourceAcc === destAcc) {
        showToast('Domain Rule Error', 'Self-transfer is prohibited. Source and destination must differ.', 'warning');
        return;
      }

      try {
        const res = await fetch('/api/transactions/transfer', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            sourceAccount: sourceAcc,
            destAccount: destAcc,
            amount: amount,
            referenceNote: memo
          })
        });
        const data = await res.json();

        if (res.ok) {
          showToast('Wire Transfer Executed', 
            `Rs. ${amount.toFixed(2)} transferred from ${sourceAcc} to ${destAcc} with ACID commit.`, 'success');
          closeModal('modalTransfer');
          modalTransferForm.reset();
          loadAllData();
        } else {
          const errCode = data.errorCode || 'ROLLBACK';
          showToast(`Transfer Fault [${errCode}]`, data.errorMessage || data.error, 'error');
        }
      } catch (err) {
        showToast('Network Error', err.message, 'error');
      }
    });
  }

  // 7. Create New Account Form
  const newAccForm = document.getElementById('formNewAccount');
  if (newAccForm) {
    newAccForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const type = document.getElementById('newAccType').value;
      const accNo = document.getElementById('newAccNumber').value;
      const name = document.getElementById('newAccHolder').value;
      const email = document.getElementById('newAccEmail').value;
      const phone = document.getElementById('newAccPhone').value;
      const initDeposit = parseFloat(document.getElementById('newAccDeposit').value) || 0;

      if (!accNo || !name) {
        showToast('Validation Error', 'Account Number and Holder Name are mandatory.', 'warning');
        return;
      }

      try {
        const payload = {
          accountNumber: accNo,
          holderName: name,
          email: email,
          phone: phone,
          accountType: type,
          initialDeposit: initDeposit
        };

        if (type === 'SAVINGS') {
          payload.interestRate = 4.25;
        } else if (type === 'CURRENT') {
          payload.overdraftLimit = 50000.0;
        } else if (type === 'FIXED_DEPOSIT') {
          payload.interestRate = 7.5;
          payload.tenureMonths = 12;
        }

        const res = await fetch('/api/accounts', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });
        const data = await res.json();

        if (res.ok) {
          showToast('Account Created', `Provisioned new ${type} account for ${name} (${accNo})`, 'success');
          closeModal('modalNewAccount');
          newAccForm.reset();
          loadAllData();
        } else {
          showToast('Creation Fault', data.errorMessage || data.error, 'error');
        }
      } catch (err) {
        showToast('Network Error', err.message, 'error');
      }
    });
  }
}
