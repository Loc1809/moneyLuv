package org.rest.model;

import javax.persistence.*;

@Entity
@TableGenerator(name = "tableGeneratorBank", table = "id_generator", pkColumnName = "entity",
        valueColumnName = "next_id", pkColumnValue = "Bank", allocationSize = 1)
@Table(name = "bank_info")
public class BankInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "tableGeneratorBank")
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

    @JoinColumn(name = "user", nullable = true)
    int user;

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

    public void setUser(int userId) {
        this.user = userId;
    }
}
