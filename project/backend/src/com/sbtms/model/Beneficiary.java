package com.sbtms.model;

/**
 * Beneficiary entity for pre-approved inter-account wire transfers.
 * 
 * @author Anish Vyapari (Roll No: 25CA1012 | PRN: DY25ENGU0AIM012)
 */
public class Beneficiary {
    private final int id;
    private final String sourceAccount;
    private final String beneficiaryAccount;
    private final String beneficiaryName;
    private final String bankIfsc;
    private final double maxLimit;
    private final String addedAt;

    public Beneficiary(int id, String sourceAccount, String beneficiaryAccount, 
                       String beneficiaryName, String bankIfsc, double maxLimit, String addedAt) {
        this.id = id;
        this.sourceAccount = sourceAccount;
        this.beneficiaryAccount = beneficiaryAccount;
        this.beneficiaryName = beneficiaryName;
        this.bankIfsc = bankIfsc;
        this.maxLimit = maxLimit;
        this.addedAt = addedAt;
    }

    public Beneficiary(String sourceAccount, String beneficiaryAccount, String beneficiaryName, String bankIfsc, double maxLimit) {
        this(0, sourceAccount, beneficiaryAccount, beneficiaryName, bankIfsc, maxLimit, null);
    }

    public int getId() { return id; }
    public String getSourceAccount() { return sourceAccount; }
    public String getBeneficiaryAccount() { return beneficiaryAccount; }
    public String getBeneficiaryName() { return beneficiaryName; }
    public String getBankIfsc() { return bankIfsc; }
    public double getMaxLimit() { return maxLimit; }
    public String getAddedAt() { return addedAt; }
}
