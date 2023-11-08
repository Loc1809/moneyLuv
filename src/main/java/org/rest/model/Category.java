package org.rest.model;

import javax.persistence.*;

@Entity
@TableGenerator(name = "tableGeneratorCategory", table = "id_generator", pkColumnName = "entity",
        valueColumnName = "next_id", pkColumnValue = "Category", allocationSize = 1)
@Table(name = "category")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "tableGeneratorCategory")
    @Column(name = "id")
    int id;

    @Column(name = "name")
    String name;

    @Column(name = "type")
    int type;

    @JoinColumn(name = "parent_type", nullable = true)
    int parent;

    @JoinColumn(name = "user", nullable = true)
    int user;

    public Category() {
    }

    public Category(int id, String name, int type, int user) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.user = user;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }
}
