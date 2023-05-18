package ch.uzh.ifi.hase.soprafs23.controller;

import ch.uzh.ifi.hase.soprafs23.SpooncularAPI.*;
import ch.uzh.ifi.hase.soprafs23.entity.FullIngredient;
import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.constant.GroupState;
import ch.uzh.ifi.hase.soprafs23.entity.Ingredient;
import ch.uzh.ifi.hase.soprafs23.SpooncularAPI.IngredientInfo;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.repository.FullIngredientRepository;
import ch.uzh.ifi.hase.soprafs23.service.GroupService;
import ch.uzh.ifi.hase.soprafs23.service.RecipeService;
import ch.uzh.ifi.hase.soprafs23.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.ArrayList;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(APIController.class) //bc we focus on testing the web layer
@Import(RestTemplateConfig.class)
public class APIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GroupService groupService;

    @MockBean
    private UserService userService;

    @MockBean
    private APIService apiService;

    @MockBean
    private RecipeService recipeService;

    @MockBean
    private IngredientInfo ingredientInfo;

    @MockBean
    private FullIngredientRepository fullIngredientRepository;

    private Group testGroup;
    private User testUser;
    private Recipe testRecipe;
    private RecipeInfo testRecipeInfo;
    private RecipeDetailInfo testRecipeDetails;
    private IngredientInfo testIngredientInfo;
    private FullIngredient testFullIngredient;

    @BeforeEach
    public void setUp() {
        testGroup = new Group();
        testGroup.setId(1L);
        testGroup.setHostId(1L);

        testUser = new User();
        testUser.setId(1L);

        testIngredientInfo = new IngredientInfo();
        List<IngredientInfo> ingredientsUsed = new ArrayList<>();
        testIngredientInfo.setName("Tomato");
        ingredientsUsed.add(testIngredientInfo);

        List<IngredientInfo> ingredientsMissed = new ArrayList<>();
        testIngredientInfo.setName("Cheese");
        ingredientsMissed.add(testIngredientInfo);

        testRecipe = new Recipe();
        testRecipe.setId(1);
        testRecipe.setReadyInMinutes(30);

        testRecipeInfo = new RecipeInfo();
        testRecipeInfo.setId(1L);
        testRecipeInfo.setTitle("Test Recipe");
        testRecipeInfo.setUsedIngredients(ingredientsUsed);
        testRecipeInfo.setMissedIngredients(ingredientsMissed);

        List<RecipeInfo> recipesList = List.of(testRecipeInfo);

        testRecipeDetails = new RecipeDetailInfo();
        testRecipeDetails.setReadyInMinutes(30);

        testFullIngredient = new FullIngredient();
        testFullIngredient.setId(1L);
        testFullIngredient.setName("apple");

        List<FullIngredient> ingredientsList = new ArrayList<>();
        ingredientsList.add(testFullIngredient);

        doReturn(1L).when(userService).getUseridByToken(Mockito.any());
        doReturn(ingredientsList).when(fullIngredientRepository).findByNameContainingIgnoreCase(Mockito.any());
        doReturn(false).when(fullIngredientRepository).existsByQuery(Mockito.any());

        doReturn(testGroup).when(groupService).getGroupById(Mockito.any());
        doReturn(testUser).when(userService).getUserById(Mockito.any());
        doReturn(1L).when(userService).getUseridByToken(Mockito.any());
        doReturn(recipesList).when(apiService).getRecipe(Mockito.any());

        doReturn(testRecipe).when(recipeService).findByExternalRecipeIdAndGroupId(Mockito.any(), Mockito.any());
        doReturn(testRecipeDetails).when(apiService).getRecipeDetails(Mockito.any());
    }


    @Test
    public void getGroupRecipe_success_200() throws Exception {
        when(groupService.getGroupById(1L)).thenReturn(testGroup);
        testGroup.setGroupState(GroupState.RECIPE);

        MockHttpServletRequestBuilder getRequest = get("/groups/{groupId}/result", 1L)
                .header("X-Token", "valid-token")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest).andExpect(status().is2xxSuccessful());

        verify(groupService, times(1)).changeGroupState(1L, GroupState.FINAL);
    }


    @Test
    public void getGroupRecipe_unauthorizedUser_401() throws Exception {
        doReturn(0L).when(userService).getUseridByToken(Mockito.any());

        mockMvc.perform(get("/groups/{groupId}/result", 1L)
                        .header("X-Token", "invalid-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getGroupRecipe_noRecipesFound_404() throws Exception {
        testGroup.setGroupState(GroupState.RECIPE);
        List<RecipeInfo> emptyRecipeList = new ArrayList<>();
        doReturn(emptyRecipeList).when(apiService).getRecipe(Mockito.any());

        mockMvc.perform(get("/groups/{groupId}/result", 1L)
                        .header("X-Token", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    @Test
    public void getGroupRecipe_withDetailsFetched() throws Exception {
        testGroup.setGroupState(GroupState.RECIPE);
        testRecipeDetails.setInstructions("Test instructions");
        testRecipeDetails.setImage("test-image.jpg");
        doReturn(testRecipeDetails).when(apiService).getRecipeDetails(Mockito.any());

        mockMvc.perform(get("/groups/{groupId}/result", 1L)
                        .header("X-Token", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testRecipe.getId()))
                .andExpect(jsonPath("$[0].title").value(testRecipe.getTitle()))
                .andExpect(jsonPath("$[0].usedIngredients[0]").value(testIngredientInfo.getName()))
                .andExpect(jsonPath("$[0].missedIngredients[0]").value(testIngredientInfo.getName()))
                .andExpect(jsonPath("$[0].instructions").value(testRecipeDetails.getInstructions()))
                .andExpect(jsonPath("$[0].image").value(testRecipeDetails.getImage()))
                .andExpect(jsonPath("$[0].readyInMinutes").value(testRecipe.getReadyInMinutes()))
                .andExpect(jsonPath("$[0].groupId").value(testGroup.getId()));
    }

    @Test
    public void getGroupRecipe_groupNotFound() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found")) // TODO: not sure if this is cheating though! Had difficulties with calling the getGroupId otherwise
                .when(groupService).getGroupById(Mockito.any());


        mockMvc.perform(get("/groups/{groupId}/result", 1L)
                        .header("X-Token", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

//
//    @Test
//    public void getRandomRecipe_returnsUnauthorizedStatusCode401() throws Exception {
//        // Set up mocks for the unauthorized case
//        doReturn(0L).when(userService).getUseridByToken("unauthorized-token");
//
//        // Perform the test
//        mockMvc.perform(get("/groups/{groupId}/result", 1L)
//                        .header("X-Token", "unauthorized-token")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isUnauthorized()); // 401
//    }

    @Test
    void getAllIngredients_validRequest_returnsListOfIngredients() throws Exception {
        String initialString = "app";

        List<FullIngredient> fullIngredients = new ArrayList<>();
        fullIngredients.add(testFullIngredient);

        // given
        given(userService.getUseridByToken(anyString())).willReturn(1L);
        given(fullIngredientRepository.findByNameContainingIgnoreCase(initialString)).willReturn(fullIngredients);

        // when/then -> perform the request and validate the response
        mockMvc.perform(get("/ingredients")
                        .header("X-Token", "valid-token")
                        .param("initialString", initialString)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0]", equalTo("apple")));
    }

    @Test
    public void getAllIngredients_returnsUnauthorizedStatusCode401() throws Exception {
        // Set up mocks for the unauthorized case
        doReturn(0L).when(userService).getUseridByToken("unauthorized-token");

        // Perform the test
        mockMvc.perform(get("/ingredients")
                        .header("X-Token", "unauthorized-token")
                        .param("initialString", "a"))
                .andExpect(status().isUnauthorized()); // 401
    }

    @Test
    void getIngredients_returnsNotFound_whenIngredientNotFound() throws Exception {
        String nonExistentIngredient = "xyz";

        // Set up mocks for the case when the ingredient is not found
        List<FullIngredient> emptyList = new ArrayList<>();
        doReturn(emptyList).when(fullIngredientRepository).findByNameContainingIgnoreCase(nonExistentIngredient);

        // Perform the test
        mockMvc.perform(get("/ingredients")
                        .header("X-Token", "valid-token")
                        .param("initialString", nonExistentIngredient)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // 404
    }
}
