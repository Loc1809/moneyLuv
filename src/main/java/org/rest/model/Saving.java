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

//    @OneToOne(mappedBy = "id", fetch = FetchType.LAZY)
//    BankInfo bankInfo;

    @Transient
    @OneToOne
    @JoinColumn(name = "bank_info")
    BankInfo bankInfo;

    @Column(name = "active")
    Boolean active;

    @Column(name = "updated_date")
    String updatedDate;

    public Saving() {
    }

    public Saving(float amount, String startDate, String endDate, String desc, User user, BankInfo bankInfo, Boolean active, String updatedDate) {
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.desc = desc;
        this.user = user;
        this.bankInfo = bankInfo;
        this.active = active;
        this.updatedDate = updatedDate;
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

    public BankInfo getBankInfo() {
        return bankInfo;
    }

    public void setBankInfo(BankInfo bankInfo) {
        this.bankInfo = bankInfo;
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }
}
