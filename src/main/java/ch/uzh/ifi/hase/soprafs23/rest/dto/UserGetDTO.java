package ch.uzh.ifi.hase.soprafs23.rest.dto;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;


public class UserGetDTO {

  private Long id;
  private String username;
  private UserStatus status;
  private String allergies;
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

  public String getSpecialDiet() {
      return specialDiet;
  }

  public void setSpecialDiet(String specialDiet) {
      this.specialDiet = specialDiet;
  }
}
