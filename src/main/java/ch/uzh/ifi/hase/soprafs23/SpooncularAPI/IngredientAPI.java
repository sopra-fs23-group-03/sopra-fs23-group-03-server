package ch.uzh.ifi.hase.soprafs23.SpooncularAPI;
import ch.uzh.ifi.hase.soprafs23.entity.Ingredient;

import java.util.List;

public class IngredientAPI {
    private int id;
    private String name;
    private String image;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
