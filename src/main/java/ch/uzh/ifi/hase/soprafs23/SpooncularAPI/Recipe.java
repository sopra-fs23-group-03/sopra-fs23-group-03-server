package ch.uzh.ifi.hase.soprafs23.SpooncularAPI;

import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.entity.User;

import java.util.List;
import javax.persistence.*;
import java.io.Serializable;

/** Recipe
 * This class is to store the final recipes, derived by the external API.
 * The groupId is optional as also the recipes derived on the go-solo are store here and naturally do not have a groupId.
 * Therefore, also the userId is nullable, as it is only used in the go solo option.
 */

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
    private String image;

    @Column(nullable = true, length = 2500)
    private String instructions;

    @Column(name = "external_recipe_id")
    private Long externalRecipeId;

    @ElementCollection
    @CollectionTable(name = "USED_INGREDIENTS", joinColumns = @JoinColumn(name="RECIPE_ID"))
    private List<String> usedIngredients;

    @ElementCollection
    @CollectionTable(name = "MISSED_INGREDIENTS")
    @Column(name = "INGREDIENT")
    private List<String> missedIngredients;

    @ManyToOne (optional = true) //TODO: need to test then for this, also handle change in service and repository layers. e.g. retrieve or save Recipe instances
    @JoinColumn(name = "group_id")
    private Group group;

    @Column(nullable = false)
    private boolean isRandomBasedOnIntolerances;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User user;


    public Recipe() {}

    public Recipe(String title, List<String> usedIngredients, Group group) {
        this.title = title;
        this.usedIngredients = usedIngredients;
        this.group = group;
        this.isRandomBasedOnIntolerances = false;
    }

    public long getId() {
        return id == null ? 0 : id;
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

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        if (instructions.length() > 2500) {
            this.instructions ="Seems like a complicated recipe! The instructions are too long, the maximum allowed length is 2500 characters. Just google the recipe and get the instructions from there :) ";
        } else {
            this.instructions = instructions;
        }
    }
    public boolean getIsRandomBasedOnIntolerances() {
        return this.isRandomBasedOnIntolerances;
    }

    public void setIsRandomBasedOnIntolerances(boolean isRandomBasedOnIntolerances) {
        this.isRandomBasedOnIntolerances = isRandomBasedOnIntolerances;
    }

    public Long getExternalRecipeId() {
        return externalRecipeId;
    }

    public void setExternalRecipeId(Long externalRecipeId) {
        this.externalRecipeId = externalRecipeId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
