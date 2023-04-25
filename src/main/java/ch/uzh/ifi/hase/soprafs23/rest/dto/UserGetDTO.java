package ch.uzh.ifi.hase.soprafs23.rest.dto;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import java.util.HashSet;
import java.util.Collections;
import java.util.Arrays;
import java.util.Set;



public class UserGetDTO {

  private Long id;
  private String username;
  private UserStatus status;
  private Set<String> allergiesSet;
  private String favoriteCuisine;
  private String specialDiet;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public UserStatus getStatus() {
    return status;
  }

  public void setStatus(UserStatus status) {
    this.status = status;
  }

  public Set<String> getAllergies() {
      return allergiesSet;
  }

    public void setAllergies(String allergies) {
        if (allergies == null || allergies.trim().isEmpty()) {
            this.allergiesSet = Collections.emptySet();
        } else {
            String[] allergyArray = allergies.split(",");
            this.allergiesSet = new HashSet<>(Arrays.asList(allergyArray));
        }
    }

    public String getFavoriteCuisine() {
      return favoriteCuisine;
  }

  public void setFavoriteCuisine(String favoriteCuisine) {
      this.favoriteCuisine = favoriteCuisine;
  }

  public String getSpecialDiet() {
      return specialDiet;
  }

  public void setSpecialDiet(String specialDiet) {
      this.specialDiet = specialDiet;
  }
}
