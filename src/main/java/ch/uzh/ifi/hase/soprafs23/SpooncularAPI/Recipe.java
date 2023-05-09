package ch.uzh.ifi.hase.soprafs23.SpooncularAPI;

import ch.uzh.ifi.hase.soprafs23.entity.Ingredient;

import java.util.List;

public class Recipe {
    private int id;
    private String title;
    private int readyInMinutes;
    double pricePerServing;

    private List<IngredientAPI> usedIngredients;

    private List<String> missedIngredients; //keep as string bc they are not in the db yet and we only display them


    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public List<IngredientAPI> getUsedIngredients() {
        return usedIngredients;
    }

    public void setUsedIngredients(List<IngredientAPI> usedIngredients) {
        this.usedIngredients = usedIngredients;
    }

    public List<String> getMissedIngredients() {
        return missedIngredients;
    }

    public void setMissedIngredients(List<String> missedIngredients) {
        this.missedIngredients = missedIngredients;
    }
}
