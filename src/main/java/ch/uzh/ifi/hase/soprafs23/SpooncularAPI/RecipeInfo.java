package ch.uzh.ifi.hase.soprafs23.SpooncularAPI;

import java.util.List;

/** RecipeInfo
 * This class stores the information from the external spoonacular API, regarding the first call.
 */

public class RecipeInfo {

    private Long id;
    private String title;
    private String image;
    private int readyInMinutes;
    private String instructions;
    private List<IngredientInfo> missedIngredients;
    private List<IngredientInfo> usedIngredients;
    private List<IngredientInfo> unusedIngredients;
    private boolean isRandomBasedOnIntolerances;

    public RecipeInfo() {
    }

    // getters and setters
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getReadyInMinutes() {
        return readyInMinutes;
    }

    public void setReadyInMinutes(int readyInMinutes) {
        this.readyInMinutes = readyInMinutes;
    }


    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public List<IngredientInfo> getMissedIngredients() {
        return missedIngredients;
    }

    public void setMissedIngredients(List<IngredientInfo> missedIngredients) {
        this.missedIngredients = missedIngredients;
    }

    public List<IngredientInfo> getUsedIngredients() {
        return usedIngredients;
    }

    public void setUsedIngredients(List<IngredientInfo> usedIngredients) {
        this.usedIngredients = usedIngredients;
    }

    public List<IngredientInfo> getUnusedIngredients() {
        return unusedIngredients;
    }

    public void setUnusedIngredients(List<IngredientInfo> unusedIngredients) {
        this.unusedIngredients = unusedIngredients;
    }

    public boolean getIsRandomBasedOnIntolerances() {
        return isRandomBasedOnIntolerances;
    }

    public void setIsRandomBasedOnIntolerances(boolean isRandomBasedOnIntolerances) {
        this.isRandomBasedOnIntolerances = isRandomBasedOnIntolerances;
    }
}
