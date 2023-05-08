package ch.uzh.ifi.hase.soprafs23.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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
    private int calculatedRating;

    @ManyToMany(mappedBy = "ingredientsSet")
    private Set<User> usersSet;

    @ManyToOne
    private Group group;

    @ElementCollection //TODO: makes sense to have own table?
    @Column(nullable = true)
    private List<String> singleUserRatings = new ArrayList<>();

    public Ingredient() {
        this.usersSet = new HashSet<>();
    }

    public Ingredient(String name) {
        this.name = name;
        this.usersSet = new HashSet<>();
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

    public int getCalculatedRating() {
        return calculatedRating;
    }

    public void setCalculatedRating(int calculatedRating) {
        this.calculatedRating = calculatedRating;
    }

    public Set<User> getUsersSet() {
        return usersSet;
    }

    public void setUsersSet(Set<User> usersSet) {
        this.usersSet = usersSet;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public List<String> getSingleUserRatings() {
        return singleUserRatings;
    }

    public void setSingleUserRatings(List<String> singleUserRatings) {
        this.singleUserRatings = singleUserRatings;
    }
}
