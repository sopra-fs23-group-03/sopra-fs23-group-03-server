package ch.uzh.ifi.hase.soprafs23.SpooncularAPI;

import ch.uzh.ifi.hase.soprafs23.entity.FullIngredient;
import ch.uzh.ifi.hase.soprafs23.entity.Ingredient;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.SpooncularAPI.IngredientAPI;
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
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PostConstruct;
import javax.persistence.criteria.CriteriaBuilder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.net.URLEncoder;

@Component
public class APIService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiKey = "56638b96d69d409cab5a0cdf9a8a1f5d";
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

    @PostConstruct
    public void init() {
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
