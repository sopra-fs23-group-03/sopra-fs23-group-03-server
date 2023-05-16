package ch.uzh.ifi.hase.soprafs23.SpooncularAPI;

import ch.uzh.ifi.hase.soprafs23.SpooncularAPI.IngredientInfo;

import java.util.List;

public class RecipeInfo {
    // this class is to store the information from the external spoonacular API, regarding the first call.

    private Long id;
    private String title;
    private String image;
    private int readyInMinutes;
    private int servings;
    private String sourceUrl;
    private int spoonacularScore;
    private String instructions;


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

    public int getServings() {
        return servings;
    }

    public void setServings(int servings) {
        this.servings = servings;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public int getSpoonacularScore() {
        return spoonacularScore;
    }

    public void setSpoonacularScore(int spoonacularScore) {
        this.spoonacularScore = spoonacularScore;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
}
