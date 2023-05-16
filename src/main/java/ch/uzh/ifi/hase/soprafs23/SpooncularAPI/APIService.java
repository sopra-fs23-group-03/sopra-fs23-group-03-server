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
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import ch.uzh.ifi.hase.soprafs23.repository.RecipeRepository;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

import java.util.*;
import java.net.URLEncoder;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component
public class APIService {
    private static final Logger logger = LoggerFactory.getLogger(APIService.class);
    @Autowired
    private GroupService groupService;
    private RecipeRepository recipeRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiKey = "56638b96d69d409cab5a0cdf9a8a1f5d";

    // TODO: give basic basic default recipe

    public Recipe getRandomRecipe(User host) { //TODO: use for Solo trip and for trouble shooting in case answer from Group is zero due to restrictions
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
        }
        catch (ResponseStatusException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found");
        }
        catch (HttpServerErrorException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authorized");
        }
    }






    public List<RecipeInfo> getRecipe(Group group) {
        Set<Ingredient> finalSetIngredients = groupService.getFinalIngredients(group);// good Ingredients
        Set<Ingredient> badSetIngredients = groupService.getBadIngredients(group);// bad Ingredients
        Set<String> allergiesGroupMembers = groupService.getGroupMemberAllergies(group); // get all intolerances

        List<String> listOfIngredients = finalSetIngredients.stream()
                .map(Ingredient::getName)
                .collect(Collectors.toList());

        String goodIngredients = listOfIngredients.stream() // we need a string to feed in API
                .map(ingredient -> URLEncoder.encode(ingredient, StandardCharsets.UTF_8))
                .collect(Collectors.joining(","));

        List<String> listOfBadIngredients = badSetIngredients.stream()
                .map(Ingredient::getName)
                .collect(Collectors.toList());

        String badIngredients = listOfBadIngredients.stream() // we need a string to feed in API
                .map(ingredient -> URLEncoder.encode(ingredient, StandardCharsets.UTF_8))
                .collect(Collectors.joining(","));

        String intolerancesString = allergiesGroupMembers.stream()
                .map(intolerance -> URLEncoder.encode(intolerance, StandardCharsets.UTF_8))
                .collect(Collectors.joining(","));


        String searchApiUrl = "https://api.spoonacular.com/recipes/complexSearch?apiKey=" + apiKey +
                "&includeIngredients=" + goodIngredients +
                "&excludeIngredients=" + badIngredients +
                "&intolerances=" + intolerancesString +
                "&number=1" +
                "&ignorePantry=true" +
                "&type=main course" +
                "&sort=max-used-ingredients" +
                "&fillIngredients=true";

        try {
            ResponseEntity<RecipeSearchResult> searchResponse = restTemplate.exchange(
                    searchApiUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<RecipeSearchResult>() {}
            );
            RecipeSearchResult searchResult = searchResponse.getBody();

            if (searchResult == null || searchResult.getResults().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NO_CONTENT, "No recipes found for the given ingredients. " +
                        "This is due to too high restrictions, e.g. your allergies matching all the given ingredients. " +
                        "We provide you now with a random recipe based only on your allergies, so you still have a cool meal to cook together!");
                // TODO: Call getRandomRecipeGroup (groupid, intolerancesString)
                // NO CONTENT 204 to make frontend display another recipe?

            }
            return searchResult.getResults();

        }
        catch (ResponseStatusException e) {
            logger.error("Error occurred while fetching recipe: " + e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Error occurred while fetching recipe: " + e.getMessage());
        }
        catch (Exception e) {
            logger.error("Unexpected error occurred while fetching recipe: " + e.getMessage(), e);
            throw new RuntimeException("Unexpected error occurred while fetching recipe: " + e.getMessage());
        }
    }


    public RecipeDetailInfo getRecipeDetails(Long externalRecipeId) {
        String informationApiUrl = "https://api.spoonacular.com/recipes/" + externalRecipeId + "/information?apiKey=" + apiKey;
        try {
            ResponseEntity<RecipeDetailInfo> infoResponse = restTemplate.exchange(informationApiUrl, HttpMethod.GET, null, new ParameterizedTypeReference<RecipeDetailInfo>() {
            });
            RecipeDetailInfo detailInfo = infoResponse.getBody();

            if (detailInfo == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No details found for the given recipe ID");
            }
            return detailInfo;
        }
        catch (ResponseStatusException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Error occurred while fetching recipe details: " + e.getMessage());
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
        }
        catch (ResponseStatusException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized.");
        }
    }

    public String getApiKey() {
        return apiKey;
    }
}
