package ch.uzh.ifi.hase.soprafs23.SpooncularAPI;
import ch.uzh.ifi.hase.soprafs23.entity.Ingredient;

import java.util.List;

public class IngredientAPI {
    private String name;

    public IngredientAPI(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}