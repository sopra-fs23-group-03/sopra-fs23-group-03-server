package ch.uzh.ifi.hase.soprafs23.SpooncularAPI;

import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.entity.Ingredient;
import ch.uzh.ifi.hase.soprafs23.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import ch.uzh.ifi.hase.soprafs23.repository.RecipeRepository;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

import java.util.*;
import java.net.URLEncoder;
import java.util.stream.Collectors;

@Component
public class APIService {

    @Autowired
    private GroupService groupService;
    private RecipeRepository recipeRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiKey = "56638b96d69d409cab5a0cdf9a8a1f5d";

    public Recipe getHostRecipe(User host) { //TODO: use for Solo trip?
        String intolerances = String.join(",", host.getAllergiesSet());
        String diet = host.getSpecialDiet();
        String cuisine = String.join(",", host.getFavoriteCuisineSet());

        String searchApiUrl = "https://api.spoonacular.com/recipes/complexSearch?apiKey=" + apiKey +
                "&intolerances=" + intolerances + "&diet=" + diet + "&cuisine=" + cuisine;

        try {
            ResponseEntity<ComplexSearchResponse> searchResponse = restTemplate.getForEntity(searchApiUrl, ComplexSearchResponse.class);
            List<Recipe> recipes = Objects.requireNonNull(searchResponse.getBody()).getResults();

            if (recipes.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Results cannot be calculated yet");
            }

            Random random = new Random();
            Recipe selectedRecipe = recipes.get(random.nextInt(recipes.size())); // Choose a random recipe from the list

            String informationApiUrl = "https://api.spoonacular.com/recipes/" + selectedRecipe.getId() + "/information?apiKey=" + apiKey;
            ResponseEntity<Recipe> informationResponse = restTemplate.getForEntity(informationApiUrl, Recipe.class);
            Recipe detailedRecipe = informationResponse.getBody();

            return detailedRecipe;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found");
            } else {
                throw e;
            }
        } catch (HttpServerErrorException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authorized");
        }
    }


    public List<RecipeInfo> getRecipe(Group group) {
        Set<Ingredient> finalSetIngredients = groupService.getFinalIngredients(group);
        List<String> listOfIngredients = finalSetIngredients.stream()
                .map(Ingredient::getName)
                .collect(Collectors.toList());

        String ingredients = listOfIngredients.stream()
                .map(ingredient -> URLEncoder.encode(ingredient, StandardCharsets.UTF_8))
                .collect(Collectors.joining(","));

        String searchApiUrl = "https://api.spoonacular.com/recipes/findByIngredients?apiKey=" + apiKey +
                "&ingredients=" + ingredients + "&number=1&ignorePantry=true&ranking=1"; //1 means maximizes used ingredients first
        try {
            ResponseEntity<List<RecipeInfo>> searchResponse = restTemplate.exchange(searchApiUrl, HttpMethod.GET, null, new ParameterizedTypeReference<List<RecipeInfo>>() {});
            List<RecipeInfo> recipeInfos = searchResponse.getBody();

            if (recipeInfos.isEmpty()) {
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "No recipes found for the given ingredients");
            }
            return recipeInfos;
        } catch (HttpClientErrorException e) {
            throw new HttpClientErrorException(e.getStatusCode(), "Error occurred while fetching recipe: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Unexpected error occurred while fetching recipe: " + e.getMessage());
        }
    }

    public RecipeDetailInfo getRecipeDetails(Long externalRecipeId) {
        String informationApiUrl = "https://api.spoonacular.com/recipes/" + externalRecipeId + "/information?apiKey=" + apiKey;
        try {
            ResponseEntity<RecipeDetailInfo> infoResponse = restTemplate.exchange(informationApiUrl, HttpMethod.GET, null, new ParameterizedTypeReference<RecipeDetailInfo>() {});
            RecipeDetailInfo detailInfo = infoResponse.getBody();

            if (detailInfo == null) {
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "No details found for the given recipe ID");
            }
            return detailInfo;
        } catch (HttpClientErrorException e) {
            throw new HttpClientErrorException(e.getStatusCode(), "Error occurred while fetching recipe details: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Unexpected error occurred while fetching recipe details: " + e.getMessage());
        }
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
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "There is no ingredient starting with those letters"); // 404 - error
            }

            return ingredientNames;
        } catch (HttpServerErrorException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized.");
        }
    }
    public String getApiKey() {
        return apiKey;
    }
}
