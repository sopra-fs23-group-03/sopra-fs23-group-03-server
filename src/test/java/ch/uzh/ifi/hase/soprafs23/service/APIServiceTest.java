package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.SpooncularAPI.*;
import ch.uzh.ifi.hase.soprafs23.entity.FullIngredient;
import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.entity.Ingredient;
import ch.uzh.ifi.hase.soprafs23.repository.FullIngredientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;

import org.springframework.web.client.RestTemplate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.springframework.core.ParameterizedTypeReference;


@SpringBootTest
public class APIServiceTest {

    @InjectMocks
    private APIService apiService;

    @Mock(name = "spoonacularRestTemplate")
    private RestTemplate spoonacularRestTemplate;

    @Mock
    private FullIngredientRepository fullIngredientRepository;

    @Mock
    private GroupService groupService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this); // This is important to initialize the @Mock and @InjectMocks annotations
        apiService = new APIService(spoonacularRestTemplate, fullIngredientRepository, groupService);
    }


    @Test
    public void testFetchAndStoreIngredients() {
        // Define the captor
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);

        // Prepare a mock response entity
        IngredientSearchResponse response = new IngredientSearchResponse();
        IngredientAPI ingredientAPI = new IngredientAPI();
        ingredientAPI.setName("testIngredient");
        response.setResults(Collections.singletonList(ingredientAPI));

        ResponseEntity<IngredientSearchResponse> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);

        when(spoonacularRestTemplate.getForEntity(
                urlCaptor.capture(),
                eq(IngredientSearchResponse.class)
        )).thenReturn(responseEntity);

        when(fullIngredientRepository.existsByQuery(anyString())).thenReturn(false);
        when(fullIngredientRepository.findByNameIgnoreCase(anyString())).thenReturn(Collections.emptyList());

        // Call the method you're testing
        String apiUrl = "https://api.spoonacular.com/recipes/complexSearch?apiKey=";
        String query = "testQuery";

        apiService.fetchAndStoreIngredients(apiUrl, query);

        // Verify interactions and capture values
        verify(spoonacularRestTemplate, times(1)).getForEntity(
                anyString(),
                eq(IngredientSearchResponse.class)
        );
        verify(fullIngredientRepository, times(1)).saveAndFlush(any(FullIngredient.class));

        // Retrieve the captured argument and make assertions on it
        String capturedUrl = urlCaptor.getValue();
        assertTrue(capturedUrl.startsWith(apiUrl + "&query="));
        assertTrue(capturedUrl.contains(query));

    }

    @Test
    public void testGetRecipe() {
        // Define the captor
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);

        // Prepare a mock Group
        Group testGroup = new Group();

        // Prepare mock Ingredient sets
        Set<Ingredient> goodIngredients = new HashSet<>(Arrays.asList(new Ingredient("tomato"), new Ingredient("cheese")));
        Set<Ingredient> badIngredients = new HashSet<>(Arrays.asList(new Ingredient("onion"), new Ingredient("garlic")));

        // Prepare mock intolerance list
        Set<String> allergies = new HashSet<>(Arrays.asList("peanuts", "gluten"));

        // Define expected API response
        RecipeSearchResult response = new RecipeSearchResult();
        RecipeInfo recipe = new RecipeInfo();
        recipe.setId(123L);
        recipe.setTitle("Test Recipe");
        response.setResults(Collections.singletonList(recipe));

        ResponseEntity<RecipeSearchResult> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);

        // Mock GroupService methods
        when(groupService.getFinalIngredients(testGroup)).thenReturn(goodIngredients);
        when(groupService.getBadIngredients(testGroup)).thenReturn(badIngredients);
        when(groupService.getGroupMemberAllergies(testGroup)).thenReturn(allergies);

        // Mock restTemplate.exchange()
        when(spoonacularRestTemplate.exchange(
                urlCaptor.capture(),
                eq(HttpMethod.GET),
                eq(null),
                eq(new ParameterizedTypeReference<RecipeSearchResult>() {})
        )).thenReturn(responseEntity);

        // Call the method you're testing
        List<RecipeInfo> recipeResults = apiService.getRecipe(testGroup);

        // Verify interactions and capture values
        verify(spoonacularRestTemplate, times(1)).exchange(
                anyString(),
                eq(HttpMethod.GET),
                eq(null),
                eq(new ParameterizedTypeReference<RecipeSearchResult>() {})
        );

        // Retrieve the captured argument and make assertions on it
        String capturedUrl = urlCaptor.getValue();
        assertTrue(capturedUrl.contains("tomato"));
        assertTrue(capturedUrl.contains("cheese"));
        assertTrue(capturedUrl.contains("peanuts"));
        assertTrue(capturedUrl.contains("gluten"));


        // Assert result
        assertNotNull(recipeResults);
        assertEquals(1, recipeResults.size());
        assertEquals(Long.valueOf(123), recipeResults.get(0).getId());
        assertEquals("Test Recipe", recipeResults.get(0).getTitle());
    }


}

