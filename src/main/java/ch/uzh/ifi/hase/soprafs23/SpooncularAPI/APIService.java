package ch.uzh.ifi.hase.soprafs23.SpooncularAPI;

import ch.uzh.ifi.hase.soprafs23.entity.FullIngredient;
import ch.uzh.ifi.hase.soprafs23.entity.Ingredient;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.entity.Ingredient;
import ch.uzh.ifi.hase.soprafs23.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import ch.uzh.ifi.hase.soprafs23.repository.FullIngredientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import ch.uzh.ifi.hase.soprafs23.repository.IngredientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import ch.uzh.ifi.hase.soprafs23.repository.RecipeRepository;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PostConstruct;
import javax.persistence.criteria.CriteriaBuilder;
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
    private final String apiKey = "355684ee05744c43a90c66aeda3fecd2";
    @Autowired
    private FullIngredientRepository fullIngredientRepository;
    @Autowired
    private IngredientRepository ingredientRepository;

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

    @PostConstruct
    public void init() {
        long count = fullIngredientRepository.count();

        // If the FullIngredient table is already populated, return and do not fetch ingredients from the API
        if (count > 0) {
            return;
        }
        // Fetch all ingredients from the API
        String ingredientsApiUrl = "https://api.spoonacular.com/food/ingredients/search?apiKey=" + apiKey + "&number=1000";

        for (char alphabet = 'a'; alphabet <= 'z'; alphabet++) {
            String query = Character.toString(alphabet);
            fetchAndStoreIngredients(ingredientsApiUrl, query);
        }
    }

    private void fetchAndStoreIngredients(String apiUrl, String query) {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String fullUrl = apiUrl + "&query=" + encodedQuery;

        ResponseEntity<IngredientSearchResponse> searchResponse = restTemplate.getForEntity(fullUrl, IngredientSearchResponse.class);
        List<IngredientAPI> ingredientsAPI = Objects.requireNonNull(searchResponse.getBody()).getResults();

        for (IngredientAPI ingredientAPI : ingredientsAPI) {
            String name = ingredientAPI.getName();
            // Save the ingredient in the FullIngredient table if not already present
            Optional<FullIngredient> existingIngredient = fullIngredientRepository.findByName(name);
            if (!existingIngredient.isPresent()) {
                FullIngredient newIngredient = new FullIngredient(name);
                fullIngredientRepository.save(newIngredient);
            }
        }
    }

    public String getApiKey() {
        return apiKey;
    }
}
