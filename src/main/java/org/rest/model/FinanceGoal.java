package org.rest.model;

import javax.persistence.*;

@Entity
@TableGenerator(name = "tableGeneratorGoal", table = "id_generator", pkColumnName = "entity",
        valueColumnName = "next_id", pkColumnValue = "Goal", allocationSize = 1)
@Table(name = "finance_goal")
public class FinanceGoal {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "tableGeneratorGoal")
    @Column(name = "id")
    int id;

    @Column(name = "amount")
    float amount;

    @Column(name = "start_date")
    String startDate;

    @Column(name = "end_date")
    String endDate;

    @Column(name = "type")
    String type;

    @OneToOne
    @JoinColumn(name = "user")
    User user;
    public FinanceGoal(int id, float amount, String startDate, String endDate, String type, User user) {
        this.id = id;
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.type = type;
        this.user = user;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getUser() {
        return user.id;
    }

//    public User getUserInfo() { return this.user; }

    public void setUser(User user) {
        this.user = user;
    }
}
