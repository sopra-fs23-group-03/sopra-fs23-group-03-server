package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.SpooncularAPI.*;
import ch.uzh.ifi.hase.soprafs23.entity.FullIngredient;
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


@SpringBootTest
public class APIServiceTest {

    @InjectMocks
    private APIService apiService;

    @Mock(name = "spoonacularRestTemplate")
    private RestTemplate spoonacularRestTemplate;

    @Mock
    private FullIngredientRepository fullIngredientRepository;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this); // This is important to initialize the @Mock and @InjectMocks annotations
        apiService = new APIService(spoonacularRestTemplate, fullIngredientRepository);
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


//
//    @Test
//    public void getRandomRecipeUserTest() throws Exception {
//        // given
//        User testUser = new User();
//        testUser.setId(1L);
//
//        Recipe recipe = new Recipe();
//        recipe.setUser(testUser);
//
//        RecipeInfo recipeInfo = new RecipeInfo();
//        recipeInfo.setId(2L);
//        recipeInfo.setTitle("New Title");
//        recipeInfo.setImage("New Image");
//        List<IngredientInfo> ingredientsMissed = new ArrayList<>();
//        recipeInfo.setMissedIngredients(ingredientsMissed);
//
//        RecipeDetailInfo detailInfo = new RecipeDetailInfo();
//        detailInfo.setReadyInMinutes(20);
//        detailInfo.setInstructions("New Instructions");
//
//        RecipeSearchResult searchResult = new RecipeSearchResult();
//        searchResult.setResults(Collections.singletonList(recipeInfo));
//
//        ResponseEntity<RecipeSearchResult> responseEntity = new ResponseEntity<>(searchResult, HttpStatus.OK);
//
//        // when
//        when(userService.getUserById(1L)).thenReturn(testUser);
//        when(recipeRepository.findByUserId(1L)).thenReturn(Optional.of(recipe));
//        when(apiService.getRecipeDetails(anyLong())).thenReturn(detailInfo);
//
//        when(restTemplate.exchange(
//                anyString(),
//                any(HttpMethod.class),
//                any(HttpEntity.class),
//                any(ParameterizedTypeReference.class)
//        )).thenReturn(responseEntity);
//
//        // call the method being tested
//        Map<String, Object> result = apiService.getRandomRecipeUser(testUser);
//
//        // then
//        assertEquals("New Title", result.get("title"));
//        assertEquals("New Image", result.get("image"));
//        assertEquals(20, result.get("readyInMinutes"));
//        assertEquals("New Instructions", result.get("instructions"));
//    }

}

