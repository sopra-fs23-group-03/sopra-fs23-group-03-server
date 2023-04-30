package ch.uzh.ifi.hase.soprafs23.SpooncularAPI;

import ch.uzh.ifi.hase.soprafs23.entity.Ingredient;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.SpooncularAPI.IngredientAPI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.persistence.criteria.CriteriaBuilder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.net.URLEncoder;

@Component
public class APIService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiKey = "355684ee05744c43a90c66aeda3fecd2";

    public Recipe getHostRecipe(User host) {
        String intolerances = String.join(",", host.getAllergiesSet());
        String diet = host.getSpecialDiet();
        String cuisine = String.join(",", host.getFavoriteCuisineSet());

        String searchApiUrl = "https://api.spoonacular.com/recipes/complexSearch?apiKey=" + apiKey +
                "&intolerances=" + intolerances + "&diet=" + diet + "&cuisine=" + cuisine;

        try {
            ResponseEntity<ComplexSearchResponse> searchResponse = restTemplate.getForEntity(searchApiUrl, ComplexSearchResponse.class);
            List<Recipe> recipes = Objects.requireNonNull(searchResponse.getBody()).getResults();

            if (recipes.isEmpty()) {
                throw new HttpClientErrorException(HttpStatus.CONFLICT, "Results cannot be calculated yet");
            }

            Random random = new Random();
            Recipe selectedRecipe = recipes.get(random.nextInt(recipes.size())); // Choose a random recipe from the list

            String informationApiUrl = "https://api.spoonacular.com/recipes/" + selectedRecipe.getId() + "/information?apiKey=" + apiKey;
            ResponseEntity<Recipe> informationResponse = restTemplate.getForEntity(informationApiUrl, Recipe.class);
            Recipe detailedRecipe = informationResponse.getBody();

            return detailedRecipe;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Group not found");
            } else {
                throw e;
            }
        } catch (HttpServerErrorException e) {
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Not authorized");
        }
    }

    public List<String> getListOfIngredients(String initialString) {
        String query = URLEncoder.encode(initialString, StandardCharsets.UTF_8);
        String ingredientsApiUrl = "https://api.spoonacular.com/food/ingredients/search?apiKey=" + apiKey + "&query=" + query + "&number=10";

        try {
            ResponseEntity<IngredientSearchResponse> searchResponse = restTemplate.getForEntity(ingredientsApiUrl, IngredientSearchResponse.class);
            List<IngredientAPI> ingredientsAPI = Objects.requireNonNull(searchResponse.getBody()).getResults();

            List<String> ingredientNames = new ArrayList<>();

            for (IngredientAPI ingredientAPI : ingredientsAPI) {
                String name = ingredientAPI.getName();
                ingredientNames.add(name);
            }

            if (ingredientNames.isEmpty()) {
                throw new HttpClientErrorException(HttpStatus.CONFLICT, "There is no ingredient starting with those letters");
            }

            return ingredientNames;
        } catch (HttpServerErrorException e) {
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "stupid sth");
        }
    }
    public String getApiKey() {
        return apiKey;
    }
}
