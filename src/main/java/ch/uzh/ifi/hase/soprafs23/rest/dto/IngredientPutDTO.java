package ch.uzh.ifi.hase.soprafs23.rest.dto;

public class IngredientPutDTO {
    private String name;

    public IngredientPutDTO(String newIngredientName) { // constructor needed for testing
    }

    //methods
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
