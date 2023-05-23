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
        // Define captor
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);

        // Prepare mock response entity
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

        // Call method
        String apiUrl = "https://api.spoonacular.com/recipes/complexSearch?apiKey=";
        String query = "testQuery";

        apiService.fetchAndStoreIngredients(apiUrl, query);

        // Verify interactions and capture values
        verify(spoonacularRestTemplate, times(1)).getForEntity(
                anyString(),
                eq(IngredientSearchResponse.class)
        );
        verify(fullIngredientRepository, times(1)).saveAndFlush(any(FullIngredient.class));

        // Retrieve the captured argument and make assertions
        String capturedUrl = urlCaptor.getValue();
        assertTrue(capturedUrl.startsWith(apiUrl + "&query="));
        assertTrue(capturedUrl.contains(query));

    }

    @Test
    public void testGetRecipe() {
        // Define captor and prepare mocks
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);

        Group testGroup = new Group();
        Set<Ingredient> goodIngredients = new HashSet<>(Arrays.asList(new Ingredient("tomato"), new Ingredient("cheese")));
        Set<Ingredient> badIngredients = new HashSet<>(Arrays.asList(new Ingredient("onion"), new Ingredient("garlic")));

        Set<String> allergies = new HashSet<>(Arrays.asList("peanuts", "gluten"));

        // Define expected API response
        RecipeSearchResult response = new RecipeSearchResult();
        RecipeInfo recipe = new RecipeInfo();
        recipe.setId(123L);
        recipe.setTitle("Test Recipe");
        response.setResults(Collections.singletonList(recipe));

        ResponseEntity<RecipeSearchResult> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);

        // Mock methods
        when(groupService.getFinalIngredients(testGroup)).thenReturn(goodIngredients);
        when(groupService.getBadIngredients(testGroup)).thenReturn(badIngredients);
        when(groupService.getGroupMemberAllergies(testGroup)).thenReturn(allergies);

        when(spoonacularRestTemplate.exchange(
                urlCaptor.capture(),
                eq(HttpMethod.GET),
                eq(null),
                eq(new ParameterizedTypeReference<RecipeSearchResult>() {})
        )).thenReturn(responseEntity);

        // Call method to test
        List<RecipeInfo> recipeResults = apiService.getRecipe(testGroup);

        // Verify interactions and capture values
        verify(spoonacularRestTemplate, times(1)).exchange(
                anyString(),
                eq(HttpMethod.GET),
                eq(null),
                eq(new ParameterizedTypeReference<RecipeSearchResult>() {})
        );

        // Retrieve captured argument and make assertions on it
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

    @Test
    public void testGetRandomRecipeGroup() {
        // Define captor and prepare mock
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        String intolerances = "peanuts,gluten";

        // Define expected API response
        RecipeSearchResult response = new RecipeSearchResult();
        RecipeInfo recipe = new RecipeInfo();
        recipe.setId(123L);
        recipe.setTitle("Test Recipe");
        response.setResults(Collections.singletonList(recipe));

        ResponseEntity<RecipeSearchResult> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);

        // Mock restTemplate.exchange()
        when(spoonacularRestTemplate.exchange(
                urlCaptor.capture(),
                eq(HttpMethod.GET),
                eq(null),
                eq(new ParameterizedTypeReference<RecipeSearchResult>() {})
        )).thenReturn(responseEntity);

        // Call method
        RecipeInfo resultRecipe = apiService.getRandomRecipeGroup(intolerances);

        // Verify interactions and capture values
        verify(spoonacularRestTemplate, times(1)).exchange(
                anyString(),
                eq(HttpMethod.GET),
                eq(null),
                eq(new ParameterizedTypeReference<RecipeSearchResult>() {})
        );

        // Retrieve the captured argument and make assertions on it
        String capturedUrl = urlCaptor.getValue();
        assertTrue(capturedUrl.contains("peanuts"));
        assertTrue(capturedUrl.contains("gluten"));

        // Assert result
        assertNotNull(resultRecipe);
        assertEquals(Long.valueOf(123), resultRecipe.getId());
        assertEquals("Test Recipe", resultRecipe.getTitle());
        assertFalse(resultRecipe.getIsRandomBasedOnIntolerances());
    }

    @Test
    public void testGetRecipeDetails() {
        // Define captor
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);

        // Prepare mock
        Long externalRecipeId = 123L;

        // Define expected response
        RecipeDetailInfo detailInfo = new RecipeDetailInfo();
        detailInfo.setTitle("Test Recipe");
        detailInfo.setReadyInMinutes(30);
        detailInfo.setInstructions("Test Instructions");

        ResponseEntity<RecipeDetailInfo> responseEntity = new ResponseEntity<>(detailInfo, HttpStatus.OK);

        // Mock restTemplate.exchange()
        when(spoonacularRestTemplate.exchange(
                urlCaptor.capture(),
                eq(HttpMethod.GET),
                eq(null),
                eq(new ParameterizedTypeReference<RecipeDetailInfo>() {})
        )).thenReturn(responseEntity);

        // Call the method
        RecipeDetailInfo resultDetailInfo = apiService.getRecipeDetails(externalRecipeId);

        // Verify interactions and capture values
        verify(spoonacularRestTemplate, times(1)).exchange(
                anyString(),
                eq(HttpMethod.GET),
                eq(null),
                eq(new ParameterizedTypeReference<RecipeDetailInfo>() {})
        );

        // Retrieve the captured argument and make assertions on it
        String capturedUrl = urlCaptor.getValue();
        assertTrue(capturedUrl.contains(externalRecipeId.toString()));

        // Assert result
        assertNotNull(resultDetailInfo);
        assertEquals("Test Recipe", resultDetailInfo.getTitle());
        assertEquals(Integer.valueOf(30), resultDetailInfo.getReadyInMinutes());
        assertEquals("Test Instructions", resultDetailInfo.getInstructions());
    }

}

