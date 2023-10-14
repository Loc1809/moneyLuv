package org.rest.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "outcome")
public class Outcome {
    @Id
    @Column(name = "id")
    int id;

    @Column(name = "amount")
    float amount;

    @Column(name = "time")
    String time;

    @Column(name = "desc")
    String desc;

    public Outcome() {
    }

    public Outcome(int id, float amount, String time, String desc) {
        this.id = id;
        this.amount = amount;
        this.time = time;
        this.desc = desc;
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
}
