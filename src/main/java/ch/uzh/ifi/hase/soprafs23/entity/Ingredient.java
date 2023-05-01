package ch.uzh.ifi.hase.soprafs23.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "INGREDIENT")
public class Ingredient implements Serializable  {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = true)
    private Long calculatedRating;

    @ManyToMany(mappedBy = "ingredientsSet")
    private Set<User> usersSet = new HashSet<>();

    public Ingredient() {
        // default constructor needed
    }
    public Ingredient(String name) {
        this.name = name;
    }

    //Methods
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCalculatedRating() {
        return calculatedRating;
    }

    public void setCalculatedRating(Long calculatedRating) {
        this.calculatedRating = calculatedRating;
    }

    public Set<User> getUsersSet() {
        return usersSet;
    }

    public void setUsersSet(Set<User> usersSet) {
        this.usersSet = usersSet;
    }
}
