package ch.uzh.ifi.hase.soprafs23.rest.dto;
import java.util.Collections;
import java.util.Arrays;
import java.util.Set;


public class UserPutDTO {

    private String username;
    private Set<String> allergies;
    private String favoriteCuisine;
    private String specialDiet;
    private String password;


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<String> getAllergies() {
        return allergies;
    }

    public void setAllergies(Set<String> allergies) {
        if (allergies != null && !allergies.isEmpty()) {
            this.allergies = Collections.singleton(String.join(",", allergies));
        } else {
            this.allergies = null;
        }
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
