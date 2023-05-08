package ch.uzh.ifi.hase.soprafs23.controller;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.constant.VotingType;
import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.entity.Invitation;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.IngredientPutDTO;
import ch.uzh.ifi.hase.soprafs23.service.GroupService;
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
import java.util.HashSet;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private GroupService groupService;

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
        user.setAllergiesSet(new HashSet<>(Arrays.asList("garlic", "nuts")));
        user.setFavoriteCuisineSet(new HashSet<>(Arrays.asList("italian", "swiss")));

        // mocks that are used in most tests
        given(userService.getUseridByToken(user.getToken())).willReturn(user.getId());
        given(userService.getUserById(user.getId())).willReturn(user);
    }

    @Test
    public void getUsers_returnsJsonArrayOfExistingUsers_whenAuthenticated() throws Exception {
        // given
        List<User> allUsers = Collections.singletonList(user);

        // mocks
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
        String anotherToken = "another token";

        // mocks
        given(userService.getUseridByToken(anotherToken)).willReturn(0L);

        // when
        MockHttpServletRequestBuilder getRequest = get("/users")
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .header("X-Token", anotherToken);

        // then
        mockMvc.perform(getRequest).andExpect(status().isUnauthorized());
        verify(userService, times(1)).getUseridByToken(anotherToken);
    }

    @Test
    public void postUsers_returnsCreatedUser_whenvalidInput() throws Exception {
        // create UserPostDTO for the request body
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername(user.getUsername());
        userPostDTO.setPassword(user.getPassword());

        // mocks
        given(userService.createUser(any())).willReturn(user);

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
        // create UserPostDTO for the request body
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername(user.getUsername());
        userPostDTO.setPassword(user.getPassword());

        // mocks
        given(userService.getUserByUsername(user.getUsername())).willReturn(user);

        // when/then -> do the request - just trying to put (=update) sth.
        MockHttpServletRequestBuilder postRequest = post("/users/{username}/login", userPostDTO.getUsername())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isOk())
                .andExpect(header().string("X-Token", notNullValue()));
    }

    @Test
    public void postUsersUsernameLogin_invalid() throws Exception {
        // create UserPostDTO for the request body
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("some username");
        userPostDTO.setPassword("some password");

        // mocks
        given(userService.getUserByUsername(userPostDTO.getUsername())).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "There is no user with this username."));

        // when/then -> do the request
        MockHttpServletRequestBuilder postRequest = post("/users/{username}/login", userPostDTO.getUsername())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isNotFound()); // has to map again with above http status
    }

    @Test
    public void testLoginUser_PasswordInvalid() throws Exception {

        // create a UserPostDTO object for the request body
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername(user.getUsername());
        userPostDTO.setPassword("wrong_password");

    // mocks
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is wrong. Check the spelling")).when(userService).correctPassword(userPostDTO.getUsername(), userPostDTO.getPassword());

        mockMvc.perform(post("/users/" + user.getUsername() + "/login").contentType(MediaType.APPLICATION_JSON).content(asJsonString(userPostDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void postUsersUserIdLogout_valid() throws Exception {
        // Create request to logout user
        MockHttpServletRequestBuilder postRequest = post("/users/{userId}/logout", user.getId())
                .header("X-Token", user.getToken())
                .contentType(MediaType.APPLICATION_JSON);

        // Send request and check response
        mockMvc.perform(postRequest)
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void postUsersUserIdLogout_notFound() throws Exception {
        Long anotherUserId = 5L;

        // given
        given(userService.getUserById(5L)).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "error message"));

        // when/then -> do the request
        mockMvc.perform(post("/users/{userId}/logout", anotherUserId)
                        .header("X-Token", "testToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void postUsersUserIdLogout_unauthorized() throws Exception {
        String anotherToken = "anotherToken";

        // given
        when(userService.getUseridByToken(anotherToken)).thenReturn(6L);

        // when/then -> do the request
        mockMvc.perform(post("/users/{userId}/logout", user.getId())
                        .header("X-Token", anotherToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException));
    }

    @Test
    public void testGetUserByIdReturns200() throws Exception {
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
                .andExpect(jsonPath("$.favoriteCuisine", containsInAnyOrder("italian", "swiss")))
                .andExpect(jsonPath("$.specialDiet", is(user.getSpecialDiet())))
                .andExpect(jsonPath("$.status", is(user.getStatus().toString())));

        // verifies that the correct calls on userService were made
        verify(userService, times(1)).getUseridByToken(user.getToken());
        verify(userService, times(1)).getUserById(user.getId());
    }

    @Test
    public void testGetUserByIdReturns404() throws Exception {
        Long secondUserId = 2L;

        // mocks the getUserById(id) method in UserService
        given(userService.getUserById(secondUserId))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "errorMessage"));

        // when
        MockHttpServletRequestBuilder getRequest = get("/users/{userId}", secondUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Token", user.getToken());

        // then
        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound());

        // verifies that the correct calls on userService were made
        verify(userService, times(1)).getUseridByToken(user.getToken());
        verify(userService, times(1)).getUserById(secondUserId);
    }

    @Test
    public void testGetUserByIdReturns401() throws Exception {
        // given
        String anotherToken = "invalid-token";

        //mocks
        Mockito.when(userService.getUseridByToken(anotherToken)).thenReturn(0L);

        // when then
        mockMvc.perform(MockMvcRequestBuilders.get("/users/" + user.getId())
                        .header("X-Token", anotherToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void putTestUpdateUser_204() throws Exception {
        // Arrange
        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("new-username");
        userPutDTO.setAllergies(Collections.singleton("Nuts"));
        userPutDTO.setFavoriteCuisine(Collections.singleton("Italian"));
        userPutDTO.setSpecialDiet("vegan");
        userPutDTO.setPassword("new-password");
        userPutDTO.setCurrentPassword(user.getPassword());

        MockHttpServletRequestBuilder requestBuilder = put("/users/{userId}", user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Token", user.getToken())
                .content(asJsonString(userPutDTO));

        // Act & Assert
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent());

        verify(userService, times(1)).updateUser(any(), any());
    }

    @Test
    public void putUsersId_whenUserDoesntExist_404() throws Exception {
        // given
        Long secondUserId = 2L;

        // mocks the getUserById(id) method in UserService
        given(userService.getUserById(secondUserId))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "errorMessage"));

        // when
        MockHttpServletRequestBuilder putRequest = put("/users/{id}", secondUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new UserPutDTO()))
                .header("X-Token", user.getToken());

        // then
        mockMvc.perform(putRequest)
                .andExpect(status().isNotFound());

        // verifies
        verify(userService, times(1)).getUserById(secondUserId);
        verify(userService, times(0)).updateUser(any(), any());
    }

    @Test
    public void PutUdateUser_whenUserUnauthorized_401() throws Exception {
        // given
        String anotherToken = "another token";

        // mocks the getUserById(id) method in UserService
        given(userService.getUseridByToken(anotherToken)).willReturn(0L);

        // when
        MockHttpServletRequestBuilder putRequest = put("/users/{id}", user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new UserPutDTO()))
                .header("X-Token", anotherToken);

        // then
        mockMvc.perform(putRequest)
                .andExpect(status().isUnauthorized());

        // verifies
        verify(userService, times(1)).getUseridByToken(anotherToken);
        verify(userService, times(0)).updateUser(any(), any());
    }

    @Test
    public void getInvitations_valid() throws Exception {
        Group group = new Group();
        group.setId(5L);
        group.setGroupName("groupname");
        group.setHostId(27L);
        group.setVotingType(VotingType.MAJORITYVOTE);

        List<Invitation> invitations = new ArrayList<>();

        Invitation invitation = new Invitation();
        invitation.setGroupId(group.getId());
        invitation.setGuestId(user.getId());
        invitations.add(invitation);

        // mocks
        given(userService.getUserById(user.getId())).willReturn(user);
        given(invitationService.getInvitationsByGuestId(user.getId())).willReturn(invitations);
        given(groupService.getGroupById(invitation.getGroupId())).willReturn(group);

        // when
        MockHttpServletRequestBuilder getRequest = get("/users/{userId}/invitations", user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Token", user.getToken());

        // then
        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(group.getId().intValue())))
                .andExpect(jsonPath("$[0].groupName", is(group.getGroupName())))
                .andExpect(jsonPath("$[0].hostId", is(group.getHostId().intValue())))
                .andExpect(jsonPath("$[0].votingType", is(group.getVotingType().toString())));

        // verify the correct calls were made
        verify(userService, times(1)).getUserById(user.getId());
        verify(userService, times(1)).getUseridByToken(user.getToken());
        verify(invitationService, times(1)).getInvitationsByGuestId(user.getId());
        verify(groupService, times(1)).getGroupById(group.getId());
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
        verify(groupService, times(0)).getGroupById(any());
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
        verify(groupService, times(0)).getGroupById(any());
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
        verify(groupService, times(0)).getGroupById(any());
    }

    @Test
    void updateUserIngredients_validRequest_returnsNoContent() throws Exception { // 204 - no content
        // create IngredientPutDTO list for the request body
        List<IngredientPutDTO> ingredientsPutDTO = new ArrayList<>();
        IngredientPutDTO ingredientPutDTO1 = new IngredientPutDTO();
        ingredientPutDTO1.setName("fish");

        IngredientPutDTO ingredientPutDTO2 = new IngredientPutDTO();
        ingredientPutDTO2.setName("beer");


        // when/then -> do the request
        String xToken = user.getToken();
        mockMvc.perform(put("/user/{userId}/ingredients", user.getId())
                        .header("X-Token", xToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(ingredientsPutDTO)))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateUserIngredients_unauthorized_returnsUnauthorized() throws Exception { // 401 - not authorized
        // create IngredientPutDTO list for the request body
        List<IngredientPutDTO> ingredientsPutDTO = new ArrayList<>();
        IngredientPutDTO ingredientPutDTO1 = new IngredientPutDTO();
        ingredientPutDTO1.setName("fish");

        IngredientPutDTO ingredientPutDTO2 = new IngredientPutDTO();
        ingredientPutDTO2.setName("beer");

        // when/then -> do the request
        String invalidToken = "invalidToken";
        mockMvc.perform(put("/user/{userId}/ingredients", user.getId())
                        .header("X-Token", invalidToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(ingredientsPutDTO)))
                .andExpect(status().isUnauthorized());

        // verify the correct calls were made
        verify(userService, times(1)).getUserById(user.getId());
        verify(userService, times(1)).getUseridByToken(invalidToken);
        verify(userService, times(0)).addUserIngredients(any(), any());
    }

    @Test
    void updateUserIngredients_userNotFound_returnsNotFound() throws Exception { // 404 - user not found
        // create IngredientPutDTO list for the request body
        List<IngredientPutDTO> ingredientsPutDTO = new ArrayList<>();
        IngredientPutDTO ingredientPutDTO1 = new IngredientPutDTO();
        ingredientPutDTO1.setName("fish");

        IngredientPutDTO ingredientPutDTO2 = new IngredientPutDTO();
        ingredientPutDTO2.setName("beer");

        // mocks
        given(userService.getUserById(user.getId())).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // when/then -> do the request
        String xToken = user.getToken();
        mockMvc.perform(put("/user/{userId}/ingredients", user.getId())
                        .header("X-Token", xToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(ingredientsPutDTO)))
                .andExpect(status().isNotFound());

        // verify the correct calls were made
        verify(userService, times(1)).getUserById(user.getId());
        verify(userService, times(0)).getUseridByToken(xToken);
        verify(userService, times(0)).addUserIngredients(any(), any());
    }


    //Helper Method to convert DTOs into a JSON strings
    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            String.format("The request body could not be created.%s", e.toString()));
        }
    }

}