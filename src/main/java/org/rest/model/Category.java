package org.rest.model;

import javax.persistence.*;
import java.util.List;
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

    @ManyToMany
    @JoinColumn(name = "child", nullable = true)
    List<Category> child;

    @OneToOne
    @JoinColumn(name = "user", nullable = true)
    User user;

    @Column(name = "active")
    Boolean active;

    @Column(name = "icon")
    String icon;

    public Category() {
    }

    public Category(String name, int type, List<Category> parent, User user, Boolean active, String icon) {
        this.name = name;
        this.type = type;
        this.child = parent;
        this.user = user;
        this.active = active;
        this.icon = icon;
    }

    public List<Category> getChild() {
        return child;
    }

    public void setChild(List<Category> child) {
        this.child = child;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
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

    public String getType() {
                if (type == 0)
            return "income";
        return "outcome";
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getUser() {
        if (user == null)
            return 0;
        return user.id;
    }

//    public User getUserInfo() { return this.user; }

    public void setUser(User user) {
        this.user = user;
    }

    public void addChildCategory(Category category) {
        this.child.add(category);
    }
}
