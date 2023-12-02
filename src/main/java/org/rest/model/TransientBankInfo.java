package org.rest.model;

import javax.persistence.*;

@Entity
@TableGenerator(name = "tableGeneratorTransientBankInfo", table = "id_generator", pkColumnName = "entity",
        valueColumnName = "next_id", pkColumnValue = "TransientBankInfo", allocationSize = 1)
@Table(name = "transient_bank")
public class TransientBankInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "tableGeneratorTransientBankInfo")
    @Column(name = "id")
    int id;

    @Column(name = "bank_name")
    String bankName;

    @Column(name = "interest_rate")
    float interestRate;

    @Column(name = "term")
    int term;

    public TransientBankInfo(String bankName, float interestRate, int term) {
        this.bankName = bankName;
        this.interestRate = interestRate;
        this.term = term;
    }

    public TransientBankInfo() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public float getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(float interestRate) {
        this.interestRate = interestRate;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }
}
