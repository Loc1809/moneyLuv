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

    @Column(name = "time")
    String time;

    @Column(name = "desc")
    String desc;

    @JoinColumn(name = "user")
    int user;

    @JoinColumn(name = "bank_info")
    int bank_info;

    public Saving() {
    }

    public Saving(int id, float amount, String time, String desc) {
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
