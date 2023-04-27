package ch.uzh.ifi.hase.soprafs23.controller;

import ch.uzh.ifi.hase.soprafs23.SpooncularAPI.*;
import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.service.GroupService;
import ch.uzh.ifi.hase.soprafs23.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
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
    @Mock
    private RestTemplate restTemplate;
    @InjectMocks
    private APIController apiController;

    private Group testGroup;
    private User testUser;
    private Recipe testRecipe;

    @BeforeEach
    public void setUp() {
        testGroup = new Group();
        testGroup.setId(1L);
        testGroup.setHostId(1L);

        testUser = new User();
        testUser.setId(1L);

        testRecipe = new Recipe();
        testRecipe.setId(1);
        testRecipe.setTitle("Test Recipe");
        testRecipe.setReadyInMinutes(30);
        testRecipe.setPricePerServing(5.00);

        doReturn(testGroup).when(groupService).getGroupById(anyLong());
        doReturn(testUser).when(userService).getUserById(anyLong());
        doReturn(1L).when(userService).getUseridByToken("valid-token");
        doReturn(testRecipe).when(apiService).getHostRecipe(testUser);
    }

    @Test
    public void getRandomRecipe_success() throws Exception {
        mockMvc.perform(get("/groups/{groupId}/result", 1L)
                        .header("X-Token", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
    @Test
    public void getHostRecipe_returnsConflictStatusCode_whenNoRecipesFound() {
        // Arrange
        User host = new User(); // Set up a User object with necessary data
        String intolerances = String.join(",", host.getAllergiesSet());
        String diet = host.getSpecialDiet();
        String cuisine = String.join(",", host.getFavoriteCuisineSet());

        String searchApiUrl = "https://api.spoonacular.com/recipes/complexSearch?apiKey=" + apiService.getApiKey() +
                "&intolerances=" + intolerances + "&diet=" + diet + "&cuisine=" + cuisine;

        ComplexSearchResponse emptyResponse = new ComplexSearchResponse();
        emptyResponse.setResults(Collections.emptyList());

        when(restTemplate.getForEntity(searchApiUrl, ComplexSearchResponse.class))
                .thenReturn(new ResponseEntity<>(emptyResponse, HttpStatus.OK));

        // Act
        try {
            apiService.getHostRecipe(host);
        } catch (HttpClientErrorException e) {
            // Assert
            assertEquals(HttpStatus.CONFLICT, e.getStatusCode());
            assertEquals("Results cannot be calculated yet", e.getMessage());
        }
    }
    @Test
    public void getHostRecipe_returnsNotFoundStatusCode_whenApiReturnsNotFound() {
        // Arrange
        User host = new User(); // Set up a User object with necessary data
        String intolerances = String.join(",", host.getAllergiesSet());
        String diet = host.getSpecialDiet();
        String cuisine = String.join(",", host.getFavoriteCuisineSet());

        String searchApiUrl = "https://api.spoonacular.com/recipes/complexSearch?apiKey=" + apiService.getApiKey() +
                "&intolerances=" + intolerances + "&diet=" + diet + "&cuisine=" + cuisine;

        when(restTemplate.getForEntity(searchApiUrl, ComplexSearchResponse.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not found"));

        // Act
        try {
            apiService.getHostRecipe(host);
        } catch (HttpClientErrorException e) {
            // Assert
            assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
            assertEquals("Group not found", e.getMessage());
        }
    }
    @Test
    public void getRandomRecipe_returnsUnauthorizedStatusCode_whenUserIsNotAuthorized() throws Exception {
        // Set up mocks for the unauthorized case
        doReturn(0L).when(userService).getUseridByToken("unauthorized-token");

        // Perform the test
        mockMvc.perform(get("/groups/{groupId}/result", 1L)
                        .header("X-Token", "unauthorized-token")
                        .contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isUnauthorized());
    }

}



