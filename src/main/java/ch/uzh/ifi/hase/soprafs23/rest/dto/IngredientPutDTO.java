package ch.uzh.ifi.hase.soprafs23.rest.dto;

public class IngredientPutDTO {
    private String name;
    private Long id;
    private Long calculatedRating;



    //methods
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCalculatedRating() {
        return calculatedRating;
    }

    public void setCalculatedRating(Long calculatedRating) {
        this.calculatedRating = calculatedRating;
    }
}
