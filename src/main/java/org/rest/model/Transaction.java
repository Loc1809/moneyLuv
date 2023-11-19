package org.rest.model;

import javax.persistence.*;

@Entity
@TableGenerator(name = "tableGeneratorTransaction", table = "id_generator", pkColumnName = "entity",
        valueColumnName = "next_id", pkColumnValue = "Transaction", allocationSize = 1)
@Table(name = "transaction")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "tableGeneratorTransaction")
    @Column(name = "id")
    int id;

    @Column(name = "amount")
    float amount;

    @Column(name = "time")
    String time;

    @Column(name = "desc")
    String desc;

    @Column(name = "direction")
    int direction;
//    0 income, 1 outcome

    @OneToOne
    @JoinColumn(name = "category")
    Category category;

    @OneToOne
    @JoinColumn(name = "user")
    User user;

    @Column(name = "active")
    Boolean active;

    public Transaction() {
    }

    public Transaction(float amount, String time, String desc, Category category, User user, int direction) {
        this.amount = amount;
        this.time = time;
        this.desc = desc;
        this.category = category;
        this.user = user;
        this.direction = direction;
    }

    public Transaction(int id, float amount, String time, String desc, Category category, User user) {
        this.id = id;
        this.amount = amount;
        this.time = time;
        this.desc = desc;
        this.category = category;
        this.user = user;
//        this.transactionSource = transactionSource;
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getUser() {
        return user.id;
    }

//    public User getUserInfo() { return this.user; }

    public void setUser(User user) {
        this.user = user;
    }


    public String getDirection() {
        if (direction == 0)
            return "income";
        return "outcome";
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    //    public int getTransactionSource() {
//        return transactionSource;
//    }
//
//    public void setTransactionSource(int transactionSource) {
//        this.transactionSource = transactionSource;
//    }
}
