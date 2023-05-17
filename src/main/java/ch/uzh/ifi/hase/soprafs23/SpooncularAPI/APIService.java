package ch.uzh.ifi.hase.soprafs23.SpooncularAPI;

import ch.uzh.ifi.hase.soprafs23.entity.FullIngredient;
import ch.uzh.ifi.hase.soprafs23.entity.Ingredient;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.entity.Ingredient;
import ch.uzh.ifi.hase.soprafs23.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import ch.uzh.ifi.hase.soprafs23.repository.FullIngredientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import ch.uzh.ifi.hase.soprafs23.repository.IngredientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
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
    @Autowired
    private FullIngredientRepository fullIngredientRepository;
    @Autowired
    private IngredientRepository ingredientRepository;

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
                    new ParameterizedTypeReference<RecipeSearchResult>() {
                    }
            );
            RecipeSearchResult searchResult = searchResponse.getBody();

            if (searchResult == null || searchResult.getResults().isEmpty()) { //TODO check with frontend if the can dispaly it like taht
                logger.info("No recipes found for the given ingredients. " +
                        "This is due to too high restrictions, e.g. your allergies matching all the given ingredients. " +
                        "We provide you now with a random recipe based only on your allergies, so you still have a cool meal to cook together!");

                RecipeInfo randomRecipe = getRandomRecipeGroup(intolerancesString);
                randomRecipe.setIsRandomBasedOnIntolerances(true); // Setting the new field to true --> can be used by frontend
                return Arrays.asList(randomRecipe);
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

    public RecipeInfo getRandomRecipeGroup(String intolerancesString) {
        String searchApiUrl = "https://api.spoonacular.com/recipes/complexSearch?apiKey=" + apiKey +
                "&intolerances=" + intolerancesString +
                "&number=1" +
                "&ignorePantry=true" +
                "&type=main course" +
                "&sort=random" +
                "&fillIngredients=true";

        try {
            ResponseEntity<RecipeSearchResult> searchResponse = restTemplate.exchange(
                    searchApiUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<RecipeSearchResult>() {
                    }
            );
            RecipeSearchResult searchResult = searchResponse.getBody();

            if (searchResult == null || searchResult.getResults().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NO_CONTENT, "Now we only send the allergies from all the guests in your group, but there is still no recipe found for it.");
            }
            RecipeInfo resultRecipe = searchResult.getResults().get(0);
            resultRecipe.setIsRandomBasedOnIntolerances(false); // Setting new field to false
            return resultRecipe;

        }
        catch (ResponseStatusException e) {
            logger.error("Error occurred while fetching random recipe: " + e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Error occurred while fetching random recipe: " + e.getMessage());
        }
        catch (Exception e) {
            logger.error("Unexpected error occurred while fetching random recipe: " + e.getMessage(), e);
            throw new RuntimeException("Unexpected error occurred while fetching random recipe: " + e.getMessage());
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


    public void fetchAndStoreIngredients(String apiUrl, String query) {
        // Fetch only if there's no record of that character in the database.
        if (!fullIngredientRepository.existsByQuery(query)) {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String fullUrl = apiUrl + "&query=" + encodedQuery;

            ResponseEntity<IngredientSearchResponse> searchResponse = restTemplate.getForEntity(fullUrl, IngredientSearchResponse.class);
            List<IngredientAPI> ingredientsAPI = Objects.requireNonNull(searchResponse.getBody()).getResults();

            for (IngredientAPI ingredientAPI : ingredientsAPI) {
                String name = ingredientAPI.getName();
                // Save the ingredient in the FullIngredient table if not already present
                Optional<FullIngredient> existingIngredient = fullIngredientRepository.findByName(name);
                if (!existingIngredient.isPresent()) {
                    FullIngredient newIngredient = new FullIngredient(name, query);
                    fullIngredientRepository.save(newIngredient);
                }
            }
        }
    }

    public String getApiKey() {
        return apiKey;
    }
}
