package ch.uzh.ifi.hase.soprafs23.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
public class IngredientPutDTO {
    private String name;

    @JsonProperty("id") //tells jackson that id here is same as in the DTO object
    private Long id;

    private String userRating;

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

    public String getUserRating() {
        return userRating;
    }

    public void setUserRating(String userRating) {
        this.userRating = userRating;
    }
}
