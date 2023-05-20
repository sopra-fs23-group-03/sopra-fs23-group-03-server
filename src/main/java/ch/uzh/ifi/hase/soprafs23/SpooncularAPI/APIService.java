package ch.uzh.ifi.hase.soprafs23.SpooncularAPI;

import ch.uzh.ifi.hase.soprafs23.entity.FullIngredient;
import ch.uzh.ifi.hase.soprafs23.entity.Ingredient;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import ch.uzh.ifi.hase.soprafs23.repository.FullIngredientRepository;
import ch.uzh.ifi.hase.soprafs23.repository.IngredientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
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
    @Autowired
    private RecipeRepository recipeRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiKey = "56638b96d69d409cab5a0cdf9a8a1f5d";
    @Autowired
    private FullIngredientRepository fullIngredientRepository;
    @Autowired
    private IngredientRepository ingredientRepository;


    public Map<String, Object> getRandomRecipeUser(User user) {  // used Map so no creating any new classes or changing existing ones
        String intolerances = String.join(",", user.getAllergiesSet());
        String diet = user.getSpecialDiet();
        String cuisine = String.join(",", user.getFavoriteCuisineSet());

        String searchApiUrl = "https://api.spoonacular.com/recipes/complexSearch?apiKey=" + apiKey +
                "&intolerances=" + intolerances +
                "&diet=" + diet +
                "&cuisine=" + cuisine +
                "&number=1" +
                "&ignorePantry=true" +
                "&type=main course" +
                "&fillIngredients=true"
                ;
        try {
            ResponseEntity<RecipeSearchResult> searchResponse = restTemplate.exchange(
                    searchApiUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<RecipeSearchResult>() {
                    }
            );
            RecipeSearchResult searchResult = searchResponse.getBody();

            if(searchResult.getResults().isEmpty()){
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No recipes found");
            }

            RecipeInfo recipeInfo = searchResult.getResults().get(0);
            RecipeDetailInfo detailInfo = getRecipeDetails(recipeInfo.getId()); //to get more Details of the recipe

            Long userId = user.getId();
            Long externalRecipeId = recipeInfo.getId();

            // Check if the recipe already exists for this user
            Optional<Recipe> existingRecipeOptional = recipeRepository.findByUserId(userId);

            Recipe recipe;
            if (existingRecipeOptional.isPresent()) {
                recipe = existingRecipeOptional.get();

                // If the existing recipe is different from the new one, update and save the recipe
                if (!recipe.getExternalRecipeId().equals(externalRecipeId)) {
                    updateRecipe(recipe, recipeInfo, detailInfo, user);
                    recipeRepository.save(recipe); // save updated recipe to db
                }

            } else {
                // Recipe doesn't exist for user, so create a new one
                recipe = new Recipe();
                // map properties from RecipeInfo to Recipe
                recipe.setUser(user);
                recipe.setExternalRecipeId(recipeInfo.getId());
                recipe.setImage(recipeInfo.getImage() != null ? recipeInfo.getImage() : "Default image URL");
                recipe.setTitle(recipeInfo.getTitle() != null ? recipeInfo.getTitle() : "Default title");

                List<String> missedIngredientsNames = recipeInfo.getMissedIngredients() != null
                        ? recipeInfo.getMissedIngredients().stream()
                        .map(IngredientInfo::getName)
                        .collect(Collectors.toList())
                        : new ArrayList<>();
                recipe.setMissedIngredients(missedIngredientsNames);

                recipe.setReadyInMinutes(detailInfo.getReadyInMinutes());
                recipe.setInstructions(detailInfo.getInstructions() != null ? detailInfo.getInstructions() : "No instructions provided");

                recipeRepository.save(recipe); // save recipe to db
            }

            Map<String, Object> response = new HashMap<>();
            response.put("title", recipeInfo.getTitle());
            response.put("image", recipe.getImage());
            response.put("readyInMinutes", recipe.getReadyInMinutes());
            response.put("instructions", recipe.getInstructions());
            response.put("missedIngredients", recipe.getMissedIngredients());

            return response;

        }
        catch (ResponseStatusException e) {
            if (e.getStatus() == HttpStatus.NOT_FOUND && e.getReason().equalsIgnoreCase("No recipes found")) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unfortunately, there were no recipes found based on the information you provided in your profile."); // 404 - not found
            }
            else {
                throw e;
            }
        }
    }

    private void updateRecipe(Recipe recipe, RecipeInfo recipeInfo, RecipeDetailInfo detailInfo, User user) {
        // map properties from RecipeInfo to Recipe
        recipe.setUser(user);
        recipe.setExternalRecipeId(recipeInfo.getId());
        recipe.setImage(recipeInfo.getImage() != null ? recipeInfo.getImage() : "Default image URL");
        recipe.setTitle(recipeInfo.getTitle() != null ? recipeInfo.getTitle() : "Default title");

        List<String> missedIngredientsNames = recipeInfo.getMissedIngredients() != null
                ? recipeInfo.getMissedIngredients().stream()
                .map(IngredientInfo::getName)
                .collect(Collectors.toList())
                : new ArrayList<>();
        recipe.setMissedIngredients(missedIngredientsNames);

        recipe.setReadyInMinutes(detailInfo.getReadyInMinutes());
        recipe.setInstructions(detailInfo.getInstructions() != null ? detailInfo.getInstructions() : "No instructions provided");
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

            if (searchResult == null || searchResult.getResults().isEmpty()) {
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
                throw new ResponseStatusException(HttpStatus.NO_CONTENT, "Now we only send only the allergies from all the guests in your group, but there is still no recipe found for it.");
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


    public RecipeDetailInfo getRecipeDetails(Long externalRecipeId) { // to fill readyInMinutes and Instructions
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

    @Transactional
    public void fetchAndStoreIngredients(String apiUrl, String query) {
        // Fetch only if there's no record of that character in the database.
        if (!fullIngredientRepository.existsByQuery(query)) {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String fullUrl = apiUrl + "&query=" + encodedQuery;

            ResponseEntity<IngredientSearchResponse> searchResponse = restTemplate.getForEntity(fullUrl, IngredientSearchResponse.class);
            List<IngredientAPI> ingredientsAPI = Objects.requireNonNull(searchResponse.getBody()).getResults();

            // synchronized block to prevent race conditions
            synchronized (this) {
                for (IngredientAPI ingredientAPI : ingredientsAPI) {
                    String name = ingredientAPI.getName();

                    // Save the ingredient in the FullIngredient table if not already present
                    List<FullIngredient> existingIngredients = fullIngredientRepository.findByNameIgnoreCase(name);

                    if (existingIngredients.isEmpty()) {
                        FullIngredient newIngredient = new FullIngredient(name, query);
                        newIngredient.setLocked(true);  // set locked to true
                        fullIngredientRepository.saveAndFlush(newIngredient);
                    }
                    else {
                        for (FullIngredient existingIngredient : existingIngredients) {
                            if (!existingIngredient.isLocked()) {
                                existingIngredient.setLocked(true);
                                fullIngredientRepository.saveAndFlush(existingIngredient);
                            }
                        }
                    }
                }
            }
        }
    }

    public String getApiKey() {
        return apiKey;
    }
}
