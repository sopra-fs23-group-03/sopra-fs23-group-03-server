package ch.uzh.ifi.hase.soprafs23.SpooncularAPI;

import ch.uzh.ifi.hase.soprafs23.entity.Ingredient;
import ch.uzh.ifi.hase.soprafs23.entity.Group;


import java.util.List;
import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "RECIPES")
public class Recipe implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipe_id", updatable = false, insertable = false)
    private Long id;
    @Column(nullable = false)
    private String title;

    @Column(nullable = true)
    private int readyInMinutes;
    @Column(nullable = true)
    double pricePerServing;

    @ElementCollection
    @CollectionTable(name = "USED_INGREDIENTS")
    private List<String> usedIngredients;

    @ElementCollection
    @CollectionTable(name = "MISSED_INGREDIENTS") //TODO: change this back?
    @Column(name = "INGREDIENT")
    private List<String> missedIngredients;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    public Recipe() {}

    public Recipe(String title, List<String> usedIngredients, Group group) {
        this.title = title;
        this.usedIngredients = usedIngredients;
        this.group = group;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getReadyInMinutes() {
        return readyInMinutes;
    }

    public void setReadyInMinutes(int readyInMinutes) {
        this.readyInMinutes = readyInMinutes;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getPricePerServing() {
        return pricePerServing;
    }

    public void setPricePerServing(double pricePerServing) {
        this.pricePerServing = pricePerServing;
    }

    public List<String> getUsedIngredients() {
        return usedIngredients;
    }

    public void setUsedIngredients(List<String> usedIngredients) {
        this.usedIngredients = usedIngredients;
    }

    public List<String> getMissedIngredients() {
        return missedIngredients;
    }

    public void setMissedIngredients(List<String> missedIngredients) {
        this.missedIngredients = missedIngredients;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}
