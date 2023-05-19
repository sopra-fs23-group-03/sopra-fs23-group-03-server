package ch.uzh.ifi.hase.soprafs23.SpooncularAPI;

import java.util.ArrayList;
import java.util.List;

/** RecipeDetailInfo
 * This class is to store all the additional Information regarding the recipe, retrieved by the second call to the external spoonacular API.
 */

public class RecipeDetailInfo {

    private Long id;
    private String title;
    private String image;
    private int readyInMinutes;
    private String instructions;
    private List<IngredientInfo> ingredients = new ArrayList<>();



    public int getReadyInMinutes() {
        return readyInMinutes;
    }

    public void setReadyInMinutes(int readyInMinutes) {
        this.readyInMinutes = readyInMinutes;
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
        this.instructions = instructions;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<IngredientInfo> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<IngredientInfo> ingredients) {
        this.ingredients = ingredients;
    }
}
