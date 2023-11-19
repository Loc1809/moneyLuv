package org.rest.model;

import javax.persistence.*;

@Entity
@TableGenerator(name = "tableGeneratorSaving", table = "id_generator", pkColumnName = "entity",
        valueColumnName = "next_id", pkColumnValue = "Saving", allocationSize = 1)
@Table(name = "saving")
public class Saving {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "tableGeneratorSaving")
    @Column(name = "id")
    int id;

    @Column(name = "amount")
    float amount;

    @Column(name = "start_date")
    String startDate;

    @Column(name = "end_date")
    String endDate;

    @Column(name = "desc")
    String desc;

    @OneToOne
    @JoinColumn(name = "user")
    User user;

    @OneToOne
    @JoinColumn(name = "bank_info")
    BankInfo bank_info;

    @Column(name = "active")
    Boolean active;

    public Saving() {
    }

    public Saving(int id, float amount, String startDate, String endDate, String desc, User user, BankInfo bank_info, Boolean active) {
        this.id = id;
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.desc = desc;
        this.user = user;
        this.bank_info = bank_info;
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public int getUser() {
        return user.id;
    }

//    public User getUserInfo() { return this.user; }


    public void setUser(User user) {
        this.user = user;
    }

    public BankInfo getBank_info() {
        return bank_info;
    }

    public void setBank_info(BankInfo bank_info) {
        this.bank_info = bank_info;
    }

    public Boolean getStatus() {
        return active;
    }

    public void setStatus(Boolean active) {
        this.active = active;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
