package ch.uzh.ifi.hase.soprafs23.controller;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserPutDTO;
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
//import org.springframework.test.web.servlet.setup.MockMvcBuilders; //unused
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    public void putUsersUsernameLogin_valid() throws Exception {
        // create new User
        User user = new User();
        user.setId(3L);
        user.setUsername("testUsername");

        //don't need comparison with internal representation here

        // when/then -> do the request - just trying to put (=update) sth.
        MockHttpServletRequestBuilder putRequest = put("/users/testUsername/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(user));

        // then
        mockMvc.perform(putRequest)
                .andExpect(status().isOk())
                .andExpect(header().string("X-Token", notNullValue()));
    }

    @Test
    public void putUsersUsernameLogin_invalid() throws Exception {
        User user = new User();
        user.setId(4L);
        user.setUsername("test");


        //try to get user with ID 4
        // We are mocking the method get userbyID here
        given(userService.getUserByUsername("testUsername")).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "There is no user with this username."));

        // when/then -> do the request
        MockHttpServletRequestBuilder putRequest = put("/users/testUsername/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(user));

        // then
        mockMvc.perform(putRequest)
                .andExpect(status().isNotFound()); // has to map again with above http status
    }

    // is this not testing the same as putUsersUsernameLogin_valid??
    @Test
    public void loginUser_DTOTest() throws Exception {
        User user = new User();
        user.setId(2L);
        user.setUsername("testName");
        user.setToken("123");
        user.setStatus(UserStatus.ONLINE);

        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("testName");

        given(userService.getUserByUsername(Mockito.any())).willReturn(user);

        MockHttpServletRequestBuilder putRequest = put("/users/"+user.getUsername()+"/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPutDTO));

        mockMvc.perform(putRequest)
                .andExpect(status().isOk())
                .andExpect(header().string("X-Token", notNullValue()));
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

        mockMvc.perform(put("/users/" + user.getUsername() + "/login").contentType(MediaType.APPLICATION_JSON).content(asJsonString(userPostDTO)))
                .andExpect(status().isBadRequest());
    }




    // TODO: add logout test?

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