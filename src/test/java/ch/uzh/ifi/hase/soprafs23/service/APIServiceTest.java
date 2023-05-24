package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.SpooncularAPI.*;
import ch.uzh.ifi.hase.soprafs23.entity.FullIngredient;
import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.entity.Ingredient;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.repository.FullIngredientRepository;
import ch.uzh.ifi.hase.soprafs23.repository.RecipeRepository;
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

    @Mock
    private RecipeRepository recipeRepository;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        apiService = new APIService(spoonacularRestTemplate, fullIngredientRepository, groupService, recipeRepository);
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

    @Test
    public void testGetRandomRecipeUser() {
        // Prepare mock user and externalRecipeId
        User user = new User();
        user.setId(123L);
        user.setAllergiesSet(new HashSet<>(Arrays.asList("nuts", "gluten")));
        user.setSpecialDiet("Vegan");
        user.setFavoriteCuisineSet(new HashSet<>(Arrays.asList("Italian", "Mexican")));

        Long externalRecipeId = 456L;

        // Define expected API responses
        RecipeSearchResult searchResult = new RecipeSearchResult();
        RecipeInfo recipeInfo = new RecipeInfo();
        recipeInfo.setId(externalRecipeId);
        recipeInfo.setTitle("Test Recipe");
        recipeInfo.setImage("Test Image");
        searchResult.setResults(Collections.singletonList(recipeInfo));

        RecipeDetailInfo detailInfo = new RecipeDetailInfo();
        detailInfo.setReadyInMinutes(0);
        detailInfo.setInstructions("Test Instructions");

        ResponseEntity<RecipeSearchResult> searchResponseEntity = new ResponseEntity<>(searchResult, HttpStatus.OK);
        ResponseEntity<RecipeDetailInfo> detailResponseEntity = new ResponseEntity<>(detailInfo, HttpStatus.OK);

        // Define a Recipe
        Recipe recipe = new Recipe();
        recipe.setUser(user);
        recipe.setExternalRecipeId(externalRecipeId);
        recipe.setTitle("Test Recipe");
        recipe.setImage("Test Image");

        // Mock interactions
        when(spoonacularRestTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                eq(null),
                eq(new ParameterizedTypeReference<RecipeSearchResult>() {})
        )).thenReturn(searchResponseEntity);

        when(spoonacularRestTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                eq(null),
                eq(new ParameterizedTypeReference<RecipeDetailInfo>() {})
        )).thenReturn(detailResponseEntity);

        when(recipeRepository.findByUserId(user.getId())).thenReturn(Optional.of(recipe));

        // Call method
        Map<String, Object> resultMap = apiService.getRandomRecipeUser(user);

        // Assert result
        assertNotNull(resultMap);
        assertEquals("Test Recipe", resultMap.get("title"));
        assertEquals("Test Image", resultMap.get("image"));
        assertEquals(0, resultMap.get("readyInMinutes")); // 0 bc this is filled in GetRecipeDetails
        assertNull(resultMap.get("instructions")); // null bc this is filled in GetRecipeDetails
        assertFalse(resultMap.get("missedIngredients") instanceof List<?>);
    }

//    @Test
//    public void testFetchIngredientsByInitialString() {
//        // Prepare
//        String initialString = "cheese";
//
//        // Define API response
//        IngredientSearchResponse searchResponse = new IngredientSearchResponse();
//        IngredientAPI ingredientAPI = new IngredientAPI();
//        ingredientAPI.setName("cheese");
//        searchResponse.setResults(Collections.singletonList(ingredientAPI));
//
//        ResponseEntity<IngredientSearchResponse> responseEntity = new ResponseEntity<>(searchResponse, HttpStatus.OK);
//
//        // mock FullIngredient
//        FullIngredient fullIngredient = new FullIngredient();
//        fullIngredient.setName("cheese");
//
//        // Mock interactions
//        when(spoonacularRestTemplate.getForEntity(
//                anyString(),
//                eq(IngredientSearchResponse.class)
//        )).thenReturn(responseEntity);
//
//        when(fullIngredientRepository.findByNameContainingIgnoreCase(initialString)).thenReturn(Collections.singletonList(fullIngredient));
//
//        // Call method
//        List<String> ingredientNames = apiService.fetchIngredientsByInitialString(initialString);
//
//        // Verify interactions and capture values
//        verify(spoonacularRestTemplate, times(6)).getForEntity(
//                anyString(),
//                eq(IngredientSearchResponse.class)
//        );
//        verify(fullIngredientRepository, times(1)).findByNameContainingIgnoreCase(initialString);
//
//        // Assert result
//        assertNotNull(ingredientNames);
//        assertEquals(1, ingredientNames.size());
//        assertEquals("cheese", ingredientNames.get(0));
//    }

}

