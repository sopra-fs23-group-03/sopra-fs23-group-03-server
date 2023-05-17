package ch.uzh.ifi.hase.soprafs23.rest.dto;

public class IngredientGetDTO {

    private Long id;
    private String name;

    private int SpoonacularId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSpoonacularId() {
        return SpoonacularId;
    }

    public void setSpoonacularId(int spoonacularId) {
        SpoonacularId = spoonacularId;
    }
}
