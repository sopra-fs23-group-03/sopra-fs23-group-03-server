package ch.uzh.ifi.hase.soprafs23.rest.dto;

public class IngredientGetDTO {

    private Long id;
    private String name;
    private Long calculatedRating;

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

    public Long getCalculatedRating() {
        return calculatedRating;
    }

    public void setCalculatedRating(Long calculatedRating) {
        this.calculatedRating = calculatedRating;
    }

}
