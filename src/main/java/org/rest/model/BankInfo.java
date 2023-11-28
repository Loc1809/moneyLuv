package org.rest.model;

import org.hibernate.validator.constraints.br.CPF;

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

    @Column(name = "bank_name")
    String bankName;

    @Column(name = "interest_rate")
    float interestRate;

    @Column(name = "term")
    int term; // store by epoch time

//    @OneToOne
//    @JoinColumn(name = "user", nullable = true)
    @Column(name = "user")
    int user;

    @Column(name = "active")
    Boolean active;

    @Column(name = "last_updated")
    String lastUpdated;
//    STORE as yyyy-MM-dd HH:mm:ss

    public BankInfo() {
    }

    public BankInfo(String bankName, float interestRate, int term, String lastUpdated) {
        this.bankName = bankName;
        this.interestRate = interestRate;
        this.term = term;
        this.active = true;
        this.lastUpdated = lastUpdated;
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

    public void setUser(int userId) {
        this.user = userId;
    }

    public int getUser() {
        return user;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void updateRate(float rate, String lastUpdated){
        this.interestRate = rate;
        this.lastUpdated = lastUpdated;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
