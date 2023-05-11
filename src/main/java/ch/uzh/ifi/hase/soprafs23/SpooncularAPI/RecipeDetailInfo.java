package ch.uzh.ifi.hase.soprafs23.SpooncularAPI;

public class RecipeDetailInfo {

    // This class in to store all the additional Information regarding the recipe, retrieved by the second call to the external spoonacular API

    private int readyInMinutes;
    private String image;
    private String instructions;


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
}
