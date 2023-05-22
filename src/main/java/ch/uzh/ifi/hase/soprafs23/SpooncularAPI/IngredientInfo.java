package ch.uzh.ifi.hase.soprafs23.SpooncularAPI;

public class IngredientInfo {

    /**
    This class maps exactly the response from the spoonacular API regarding the ingredients
     */

    private int id;
    private String name;

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
}

