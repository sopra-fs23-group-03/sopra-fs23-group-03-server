package ch.uzh.ifi.hase.soprafs23.entity;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.constant.UserVotingStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;
import java.util.List;
import java.util.HashSet;

@Entity
@Table(name = "USER")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private UserStatus status;

    @Column(nullable = true)
    @ElementCollection
    private Set<String> allergiesSet;

    @Column(nullable = true)
    @ElementCollection
    private Set<String> favoriteCuisineSet;

    @Column(nullable = true)
    private String specialDiet;

    @Column(nullable = true)
    private Long groupId;

    @Column(nullable = false)
    private UserVotingStatus votingStatus;

    @ManyToMany(cascade = CascadeType.ALL) // so all changes are applied to ingredients class which is associated
    @JoinTable(
            name = "USER_INGREDIENT",
            joinColumns = @JoinColumn(name = "user_id"), //those are the 2 foreign keys
            inverseJoinColumns = @JoinColumn(name = "ingredient_id"))
    private Set<Ingredient> ingredientsSet = new HashSet<>(); // set of ingredients objects of the corresponding user

    // Constructor
    public User() {
        this.allergiesSet = new HashSet<>();
        this.favoriteCuisineSet = new HashSet<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Set<String> getAllergiesSet() {
        return allergiesSet;
    }

    public void setAllergiesSet(Set<String> allergies) {
        this.allergiesSet = allergies;
    }

    public void addAllergy(String allergy) {
        if (allergiesSet == null) {
            allergiesSet = new HashSet<>();
        }
        allergiesSet.add(allergy);
    }

    public void removeAllergies() {
        this.allergiesSet = new HashSet<>();
    }

    public Set<String> getFavoriteCuisineSet() {
        return favoriteCuisineSet;
    }

    public void setFavoriteCuisineSet(Set<String> favoriteCuisine) {
        this.favoriteCuisineSet = favoriteCuisine;
    }

    public void addFavouriteCuisine(String favouriteCuisine) {
        if (favoriteCuisineSet == null) {
            favoriteCuisineSet = new HashSet<>();
        }
        favoriteCuisineSet.add(favouriteCuisine);
    }

    public void removeFavouriteCuisines() {
        this.favoriteCuisineSet = new HashSet<>();
    }

    public String getSpecialDiet() {
        return specialDiet;
    }

    public void setSpecialDiet(String specialDiet) {
        this.specialDiet = specialDiet;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public void addIngredient(List<Ingredient> ingredients) { // even it is a set we check double entries beforehand (for the tests)
        for (Ingredient ingredient : ingredients) {
            if (!ingredientsSet.contains(ingredient)) {
                ingredientsSet.add(ingredient);
            }
        }
    }

    public void removeIngredient(Ingredient ingredient) {
        ingredientsSet.remove(ingredient);
    }

    public Set<Ingredient> getIngredients() {
        return ingredientsSet;
    }

    public UserVotingStatus getVotingStatus() {
        return votingStatus;
    }

    public void setVotingStatus(UserVotingStatus votingStatus) {
        this.votingStatus = votingStatus;
    }
}
