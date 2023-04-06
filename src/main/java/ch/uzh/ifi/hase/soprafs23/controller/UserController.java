package ch.uzh.ifi.hase.soprafs23.controller;

import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs23.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs23.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/users")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<UserGetDTO> getAllUsers(HttpServletRequest request) {
    // check validity of token
    String token = request.getHeader("X-Token");
    if(userService.getUseridByToken(token) == 0) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("You are not authorized."));
    }

    // fetch all users in the internal representation
    List<User> users = userService.getUsers();
    List<UserGetDTO> userGetDTOs = new ArrayList<>();

    // convert each user to the API representation
    for (User user : users) {
      userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
    }
    return userGetDTOs;
  }

  @PostMapping("/users")
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public UserGetDTO createUser(@RequestBody UserPostDTO userPostDTO) {
    // convert API user to internal representation
    User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

    // create user
    User createdUser = userService.createUser(userInput);

    // convert internal representation of user back to API
    UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);

    // create HttpHeaders object, add token in response header and make it accessible to the client
    HttpHeaders headers = new org.springframework.http.HttpHeaders();
    headers.add("X-Token", createdUser.getToken());
    List<String> customHeaders = new ArrayList<String>();
    customHeaders.add("X-Token");
    headers.setAccessControlExposeHeaders(customHeaders);

    return userGetDTO;
  }

  @PutMapping("users/{username}/login")
  @ResponseStatus(HttpStatus.OK) //OK is 200
  @ResponseBody
  public UserGetDTO loginUser(@RequestBody UserPostDTO userPostDTO, @PathVariable String username){
      //get user by username
      User user = userService.getUserByUsername(username);

      //get password which belongs to given username and check if provided password is same
      User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO); //convert info to internal representation
      userService.correctPassword(username, userInput.getPassword());

      //set online
      userService.login(user);

      return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user); //send back user
  }

  @PostMapping("users/{userId}/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT) //OK is 200
  @ResponseBody
  public UserGetDTO logoutUser(@PathVariable Long id){
      //get user by id
      User user = userService.getUserById(id);

      //set offline
      userService.logout(user.getId());

      return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user); //send back user
  }

  @PutMapping("/users/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updateUser(@PathVariable Long id,
                         @RequestBody UserPutDTO userPutDTO,
                         HttpServletRequest request)
  {
      userService.getUserById(id);
      Long tokenId = userService.getUserByToken(request.getHeader("X-Token"));
      if(tokenId != id) {
          throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("You are not authorized to make changes to this profile."));
      }

      userService.updateUser(id, userPutDTO);
  }
}
