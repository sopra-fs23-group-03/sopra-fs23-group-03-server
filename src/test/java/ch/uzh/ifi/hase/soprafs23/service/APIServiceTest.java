package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.SpooncularAPI.*;
import ch.uzh.ifi.hase.soprafs23.constant.GroupState;
import ch.uzh.ifi.hase.soprafs23.constant.VotingType;
import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.entity.Ingredient;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.repository.FullIngredientRepository;
import ch.uzh.ifi.hase.soprafs23.repository.GroupRepository;
import ch.uzh.ifi.hase.soprafs23.repository.IngredientRepository;
import ch.uzh.ifi.hase.soprafs23.repository.RecipeRepository;
import javassist.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;

import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(SpringExtension.class)
@WebMvcTest(APIController.class) //bc we focus on testing the web layer
@Import(RestTemplateConfig.class)
public class APIServiceTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private GroupRepository groupRepository;

    @MockBean
    private IngredientRepository ingredientRepository;

    @MockBean
    private FullIngredientRepository fullIngredientRepository;

    @MockBean
    private GroupService groupService;

    @MockBean
    private RecipeService recipeService;

    @MockBean
    private JoinRequestService joinRequestService;

    @MockBean
    private InvitationService invitationService;

    @MockBean
    private RecipeRepository recipeRepository;

    @MockBean
    private UserService userService;

    @MockBean
    private APIService apiService;


    @BeforeEach
    public void setup() {
        // given
        User testUser = new User();
        testUser.setId(1L);

        // when
        when(userService.getUseridByToken("valid-token")).thenReturn(1L);
        when(userService.getUserById(1L)).thenReturn(testUser);

    }

    @Test
    public void getRandomRecipeUserTest_including_updateRecipeTest() throws Exception {
        // given
        User testUser = new User();
        testUser.setId(1L);

        Recipe recipe = new Recipe();
        recipe.setUser(testUser);

        RecipeInfo recipeInfo = new RecipeInfo();
        recipeInfo.setId(2L);
        recipeInfo.setTitle("New Title");
        recipeInfo.setImage("New Image");
        List<IngredientInfo> ingredientsMissed = new ArrayList<>();
        recipeInfo.setMissedIngredients(ingredientsMissed);

        RecipeDetailInfo detailInfo = new RecipeDetailInfo();
        detailInfo.setReadyInMinutes(20);
        detailInfo.setInstructions("New Instructions");

        Map<String, Object> recipeMap = new HashMap<>();
        recipeMap.put("id", 2L);
        recipeMap.put("title", "New Title");
        recipeMap.put("image", "New Image");
        recipeMap.put("readyInMinutes", 20);
        recipeMap.put("instructions", "New Instructions");

        // when
        when(userService.getUserById(1L)).thenReturn(testUser);
        when(apiService.getRandomRecipeUser(testUser)).thenReturn(recipeMap);
        when(recipeRepository.findByUserId(1L)).thenReturn(Optional.of(recipe));

        MockHttpServletRequestBuilder getRequest = get("/users/{userId}/solo/result", 1L)
                .header("X-Token", "valid-token")
                .contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.title", is("New Title")))
                .andExpect(jsonPath("$.image", is("New Image")))
                .andExpect(jsonPath("$.readyInMinutes", is(20)))
                .andExpect(jsonPath("$.instructions", is("New Instructions")));
    }

}
