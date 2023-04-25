package ch.uzh.ifi.hase.soprafs23.rest.dto;

public class UserPutDTO {

    private String username;
    private String allergies;
    private String favoriteCuisine;
    private String specialDiet;
    private String password;


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getFavoriteCuisine() {
        return favoriteCuisine;
    }

    public void setFavoriteCuisine(String favoriteCuisine) {
        this.favoriteCuisine = favoriteCuisine;
    }

    public String getSpecialDiet(){
        return specialDiet;
    }

    public void setSpecialDiet(String specialDiet) {
        this.specialDiet = specialDiet;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}