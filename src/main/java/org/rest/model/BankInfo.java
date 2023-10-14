package org.rest.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "bank_info")
public class BankInfo {
    @Id
    @Column(name = "id")
    int id;

    @Column(name = "account_number")
    String accountNumber;

    @Column(name = "bank_name")
    String bankName;

    @Column(name = "interest_rate")
    float interestRate;

    @Column(name = "term")
    int term; // store by epoch time

    public BankInfo() {
    }

    public BankInfo(int id, String accountNumber, String bankName, float interestRate, int term) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.bankName = bankName;
        this.interestRate = interestRate;
        this.term = term;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
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
