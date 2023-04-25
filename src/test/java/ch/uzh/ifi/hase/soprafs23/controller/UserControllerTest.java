package ch.uzh.ifi.hase.soprafs23.controller;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.entity.Invitation;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs23.service.InvitationService;
import ch.uzh.ifi.hase.soprafs23.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
//import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put; //unused
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network.
 * This tests if the UserController works.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UserService userService;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockBean
  private InvitationService invitationService;

  private User user;

  @BeforeEach
  private void setup() {
    user = new User();
    user.setId(1L);
    user.setUsername("firstUsername");
    user.setPassword("firstPassword");
    user.setToken("firstToken");
    user.setStatus(UserStatus.ONLINE);

    // mocks the getUserIdByTokem(token) method in UserService
    given(userService.getUseridByToken(user.getToken())).willReturn(user.getId());
  }

  @Test
  public void getUsers_returnsJsonArrayOfExistingUsers_whenAuthenticated() throws Exception {
    // given
    List<User> allUsers = Collections.singletonList(user);

    // this mocks the UserService -> we define above what the userService should
    // return when getUsers() is called
    given(userService.getUsers()).willReturn(allUsers);

    // when
    MockHttpServletRequestBuilder getRequest = get("/users")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .header("X-Token", user.getToken());

    // then
    mockMvc.perform(getRequest).andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].username", is(user.getUsername())))
        .andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
  }

  @Test
  public void getUsers_returnsErrorUNAUTHORIZED_whenNotAuthenticated() throws Exception {
    // mocks the getUserIdByTokem(token) method in UserService
    given(userService.getUseridByToken("newToken")).willReturn(0L);

    // when
    MockHttpServletRequestBuilder getRequest = get("/users")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .header("X-Token", "newToken");

    // then
    mockMvc.perform(getRequest).andExpect(status().isUnauthorized());
    verify(userService, times(1)).getUseridByToken("newToken");
  }

  @Test
  public void postUsers_returnsCreatedUser_whenvalidInput() throws Exception {
    // create UserPostDTO for the request body
    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setUsername(user.getUsername());
    userPostDTO.setPassword(user.getPassword());

    // mocks the createUser(user) method in UserService
    given(userService.createUser(Mockito.any(User.class))).willReturn(user);

    // when
    MockHttpServletRequestBuilder postRequest = post("/users")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(asJsonString(userPostDTO));

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isCreated())
        .andExpect(header().string("X-Token", notNullValue()))
        .andExpect(jsonPath("$.id", is(user.getId().intValue())))
        .andExpect(jsonPath("$.username", is(user.getUsername())))
        .andExpect(jsonPath("$.status", is(user.getStatus().toString())));

    // verify that the correct calls on UserService were made
    verify(userService, times(1)).createUser(Mockito.any(User.class));
  }


  @Test
  public void postUsers_returnsErrorConflict_whenInvalidInput() throws Exception {
    // create UserPostDTO for the request body
    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setUsername(user.getUsername());
    userPostDTO.setPassword(user.getPassword());

    // mocks the createUser(user) method in UserService
    given(userService.createUser(Mockito.any(User.class)))
        .willThrow(new ResponseStatusException(HttpStatus.CONFLICT, "error message"));

    // when
    MockHttpServletRequestBuilder postRequest = post("/users")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(asJsonString(userPostDTO));

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isConflict());

    // verify that the correct calls on UserService were made
    verify(userService, times(1)).createUser(Mockito.any(User.class));
  }


    @Test
    public void postUsersUsernameLogin_valid() throws Exception {
        // create new User
        User user = new User();
        user.setId(3L);
        user.setUsername("testUsername");
        user.setToken("testToken");

        given(userService.getUserByUsername(user.getUsername())).willReturn(user);

        // when/then -> do the request - just trying to put (=update) sth.
        MockHttpServletRequestBuilder postRequest = post("/users/testUsername/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(user));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isOk())
                .andExpect(header().string("X-Token", notNullValue()));
    }

    @Test
    public void postUsersUsernameLogin_invalid() throws Exception {
        User user = new User();
        user.setId(4L);
        user.setUsername("test");


        //try to get user with ID 4
        // We are mocking the method get userbyID here
        given(userService.getUserByUsername("testUsername")).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "There is no user with this username."));

        // when/then -> do the request
        MockHttpServletRequestBuilder postRequest = post("/users/testUsername/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(user));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isNotFound()); // has to map again with above http status
    }

    @Test
    public void testLoginUser_PasswordInvalid() throws Exception {
        // create a User object
        User user = new User();
        user.setId(1L);
        user.setUsername("name");
        user.setPassword("word");

        // create a UserPostDTO object for the request body
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("name");
        userPostDTO.setPassword("wrong_password");


        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is wrong. Check the spelling")).when(userService).correctPassword(Mockito.any(String.class), Mockito.any(String.class));

        mockMvc.perform(post("/users/" + user.getUsername() + "/login").contentType(MediaType.APPLICATION_JSON).content(asJsonString(userPostDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void postUsersUserIdLogout_valid() throws Exception {
        // Set up mock userService method
        given(userService.getUseridByToken("testToken")).willReturn(3L);

        // Create request to logout user
        MockHttpServletRequestBuilder postRequest = post("/users/3/logout")
                .header("X-Token", "testToken")
                .contentType(MediaType.APPLICATION_JSON);

        // Send request and check response
        mockMvc.perform(postRequest)
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }
    @Test
    public void postUsersUserIdLogout_notFound() throws Exception {
        // given
        given(userService.getUseridByToken("testToken")).willReturn(0L);

        // when/then -> do the request
        mockMvc.perform(post("/users/1/logout")
                        .header("X-Token", "testToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void postUsersUserIdLogout_unauthorized() throws Exception {
        // given
        when(userService.getUseridByToken("testToken")).thenReturn(0L);

        // when/then -> do the request
        mockMvc.perform(post("/users/0/logout")
                        .header("X-Token", "testToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException));
    }

    @Test
    public void testGetUserByIdReturns200() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("firstUsername");
        user.setPassword("firstPassword");
        user.setToken("firstToken");
        user.setStatus(UserStatus.ONLINE);
        user.setAllergiesSet(new HashSet<>(Arrays.asList("garlic", "nuts")));
        // mocks the getUserById(id) method in UserService
        given(userService.getUserById(user.getId())).willReturn(user);

        // when
        MockHttpServletRequestBuilder getRequest = get("/users/{userId}", user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Token", user.getToken());

        // then
        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user.getId().intValue())))
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.allergies", containsInAnyOrder("garlic", "nuts")))
                .andExpect(jsonPath("$.favoriteCuisine", is(user.getFavoriteCuisine())))
                .andExpect(jsonPath("$.specialDiet", is(user.getSpecialDiet())))
                .andExpect(jsonPath("$.status", is(user.getStatus().toString())));

        // verifies that the correct calls on userService were made
        verify(userService, times(1)).getUseridByToken(user.getToken());
        verify(userService, times(1)).getUserById(user.getId());
    }
    @Test
    public void testGetUserByIdReturns404() throws Exception {
        Long secondUserId = 2L;
        String errorMessage = String.format("User with id %s does not exist.", secondUserId);

        // mocks the getUserById(id) method in UserService
        given(userService.getUserById(secondUserId))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage));

        // when
        MockHttpServletRequestBuilder getRequest = get("/users/{userId}", secondUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Token", user.getToken());

        // then
        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound());
        //.andExpect(jsonPath("$.message", is(errorMessage)));

        // verifies that the correct calls on userService were made
        verify(userService, times(1)).getUseridByToken(user.getToken());
        verify(userService, times(1)).getUserById(secondUserId);
    }

    @Test
    public void testGetUserByIdReturns401() throws Exception {
        Long userId = 1L;
        String token = "invalid-token";
        Mockito.when(userService.getUseridByToken(token)).thenReturn(0L);
        mockMvc.perform(MockMvcRequestBuilders.get("/users/" + userId)
                        .header("X-Token", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }
    @Test
    public void putTestUpdateUser_204() throws Exception {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setToken("valid-token");
        user.setPassword("test-password");
        Mockito.when(userService.getUserById(1L)).thenReturn(user);
        Mockito.when(userService.getUseridByToken("valid-token")).thenReturn(1L);

        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("new-username");
        userPutDTO.setAllergies(Collections.singleton("Nuts"));
        userPutDTO.setFavoriteCuisine("Italian");
        userPutDTO.setSpecialDiet("vegan");
        userPutDTO.setPassword("new-password");

        MockHttpServletRequestBuilder requestBuilder = put("/users/1?currentPassword=test-password")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Token", "valid-token")
                .content(objectMapper.writeValueAsString(userPutDTO));

        // Act & Assert
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent());
    }

    @Test
    public void putUsersId_whenUserDoesntExist_404() throws Exception {
        //String newAllergies = "secondAllergies";
        String newUsername = "secondUsername";
        String newFavoriteCuisine = "secondFavoriteCuisine";
        String newSpecialDiet = "secondSpecialDiet";
        String newPassword = "secondPassword";

        Long secondUserId = 2L;
        String errorMessage = String.format("User with id %s does not exist.", secondUserId);

        // create UserPutDTO for the request body
        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername(newUsername);
        userPutDTO.setAllergies(Collections.singleton("Nuts"));
        userPutDTO.setFavoriteCuisine(newFavoriteCuisine);
        userPutDTO.setSpecialDiet(newSpecialDiet);
        userPutDTO.setPassword(newPassword);

        // mocks the getUserById(id) method in UserService
        given(userService.getUserById(secondUserId))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage));

        // when
        MockHttpServletRequestBuilder putRequest = put("/users/{id}?currentPassword=test-password", secondUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPutDTO))
                .header("X-Token", user.getToken());

        // then
        mockMvc.perform(putRequest)
                .andExpect(status().isNotFound());

        // verifies that there were the correct calls on userService
        verify(userService, times(1)).getUserById(secondUserId);
    }


    @Test
    public void PutUdateUser_whenUserUnauthorized_401() throws Exception {
        Long userId = 1L;
        String token = "invalid-token";
        User user = new User();
        user.setId(userId);
        Mockito.when(userService.getUserById(userId)).thenReturn(user); // modify the mock response to return null
        Mockito.when(userService.getUseridByToken(token)).thenReturn(0L);
        mockMvc.perform(put("/users/" + userId + "?currentPassword=test-password")
                        .header("X-Token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"newUsername\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }
    @Test
    public void getInvitations_valid() throws Exception {
        List<Invitation> invitations = new ArrayList<>();

        Invitation invitation = new Invitation();
        invitation.setGroupId(5L);
        invitation.setGuestId(user.getId());
        invitations.add(invitation);

        // mocks
        given(userService.getUserById(user.getId())).willReturn(user);
        given(invitationService.getInvitationsByGuestId(user.getId())).willReturn(invitations);

        // when
        MockHttpServletRequestBuilder getRequest = get("/users/{userId}/invitations", user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Token", user.getToken());

        // then
        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].groupId", is(invitation.getGroupId().intValue())));

        // verify the correct calls were made
        verify(userService, times(1)).getUserById(user.getId());
        verify(userService, times(1)).getUseridByToken(user.getToken());
        verify(invitationService, times(1)).getInvitationsByGuestId(user.getId());
    }

    @Test
    public void getInvitations_noOpenInvitations() throws Exception {
        List<Invitation> invitations = new ArrayList<>();

        // mocks
        given(userService.getUserById(user.getId())).willReturn(user);
        given(invitationService.getInvitationsByGuestId(user.getId())).willReturn(invitations);

        // when
        MockHttpServletRequestBuilder getRequest = get("/users/{userId}/invitations", user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Token", user.getToken());

        // then
        mockMvc.perform(getRequest)
                .andExpect(status().isNoContent());

        // verify the correct calls were made
        verify(userService, times(1)).getUserById(user.getId());
        verify(userService, times(1)).getUseridByToken(user.getToken());
        verify(invitationService, times(1)).getInvitationsByGuestId(user.getId());
    }

    @Test
    public void getInvitations_userNotFound() throws Exception {
        Long anotherUserId = 4L;

        // mocks
        given(userService.getUserById(anotherUserId)).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "error message"));

        // when
        MockHttpServletRequestBuilder getRequest = get("/users/{userId}/invitations", anotherUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Token", user.getToken());

        // then
        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound());

        // verify the correct calls were made
        verify(userService, times(1)).getUserById(anotherUserId);
        verify(userService, times(0)).getUseridByToken(any());
        verify(invitationService, times(0)).getInvitationsByGuestId(any());
    }

    @Test
    public void getInvitations_notValidToken() throws Exception {
      String anotherToken = "anotherToken";

      // mocks
      given(userService.getUserById(user.getId())).willReturn(user);
      given(userService.getUseridByToken(anotherToken)).willReturn(0L);

        // when
        MockHttpServletRequestBuilder getRequest = get("/users/{userId}/invitations", user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Token", anotherToken);

        // then
        mockMvc.perform(getRequest)
                .andExpect(status().isUnauthorized());

        // verify the correct calls were made
        verify(userService, times(1)).getUserById(user.getId());
        verify(userService, times(1)).getUseridByToken(anotherToken);
        verify(invitationService, times(0)).getInvitationsByGuestId(any());
    }
  /**
   * Helper Method to convert userPostDTO into a JSON string such that the input
   * can be processed
   * Input will look like this: {"name": "Test User", "username": "testUsername"}
   * 
   * @param object
   * @return string
   */
  private String asJsonString(final Object object) {
    try {
      return new ObjectMapper().writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          String.format("The request body could not be created.%s", e.toString()));
    }
  }
}