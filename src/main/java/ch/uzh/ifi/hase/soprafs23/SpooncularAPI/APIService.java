package ch.uzh.ifi.hase.soprafs23.SpooncularAPI;

import ch.uzh.ifi.hase.soprafs23.entity.Ingredient;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.service.GroupService;
import ch.uzh.ifi.hase.soprafs23.SpooncularAPI.IngredientAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
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
import java.util.stream.Collectors;

@Component
public class APIService {

    @Autowired
    private GroupService groupService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiKey = "355684ee05744c43a90c66aeda3fecd2";

    public Recipe getHostRecipe(User host) { //TODO: delete at some point bc we use getRecipe then
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

    public Recipe getRecipe(Group group) { // get recipes based on the ingredients provided by the group
        Set<Ingredient> finalSetIngredients = groupService.getFinalIngredients(group);
        List<String> listOfIngredients = finalSetIngredients.stream() // need to convert the Set into strings via List<String>
                .map(Ingredient::getName)
                .collect(Collectors.toList());

        String ingredients = String.join(",", listOfIngredients);
        String searchApiUrl = "https://api.spoonacular.com/recipes/findByIngredients?apiKey=" + apiKey +
                "&ingredients=" + ingredients + "&number=3&ignorePantry=true&ranking=2";

        try {
            ResponseEntity<List<RecipeInfo>> searchResponse = restTemplate.exchange(searchApiUrl, HttpMethod.GET, null, new ParameterizedTypeReference<>() {}); //we use this here bc response has more objects
            List<RecipeInfo> recipes = searchResponse.getBody();
            if (recipes != null && !recipes.isEmpty()) {
                RecipeInfo recipeInfo = recipes.get(0);
                Recipe recipe = new Recipe();
                recipe.setTitle(recipeInfo.getTitle());
                recipe.setUsedIngredients(recipeInfo.getUsedIngredients());
                return recipe;
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Group not found");
            }
        }

        return null;


    }


    public List<String> getListOfIngredients(String initialString) { //for frontend to be displayed in order for the users to choose from
        String query = URLEncoder.encode(initialString, StandardCharsets.UTF_8);
        String ingredientsApiUrl = "https://api.spoonacular.com/food/ingredients/search?apiKey=" + apiKey + "&query=" + query + "&number=100";

        try {
            ResponseEntity<IngredientSearchResponse> searchResponse = restTemplate.getForEntity(ingredientsApiUrl, IngredientSearchResponse.class);
            List<IngredientAPI> ingredientsAPI = Objects.requireNonNull(searchResponse.getBody()).getResults();

            List<String> ingredientNames = new ArrayList<>();

            for (IngredientAPI ingredientAPI : ingredientsAPI) {
                String name = ingredientAPI.getName();
                ingredientNames.add(name);
            }

            if (ingredientNames.isEmpty()) {
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "There is no ingredient starting with those letters"); // 404 - error
            }

            return ingredientNames;
        } catch (HttpServerErrorException e) {
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "You are not authorized.");
        }
    }
    public String getApiKey() {
        return apiKey;
    }
}
