package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.repository.UserRepository;
//import org.slf4j.Logger; // unused
//import org.slf4j.LoggerFactory; // unused
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {

  //private final Logger log = LoggerFactory.getLogger(UserService.class); // unused

  private final UserRepository userRepository;

  @Autowired
  public UserService(@Qualifier("userRepository") UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public List<User> getUsers() {
    return this.userRepository.findAll();
  }


  // creates a new user, throws CONFLICT (409) if something goes wrong
  public User createUser(User newUser) {
    newUser.setToken(UUID.randomUUID().toString());
    newUser.setStatus(UserStatus.ONLINE);

    // check that the username is still free
    checkIfUsernameExists(newUser.getUsername());

    // check if username is only latin letters
    if (!newUser.getUsername().matches("^[a-zA-Z]+$")) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "The username should only contain Latin letters (a-z, A-Z)!");
    }

    // check that username and password are not the same
    if(newUser.getUsername().equals(newUser.getPassword())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "The username and password cannot be the same!");
    }

    // save the user
    newUser = userRepository.save(newUser);
    userRepository.flush();

    return newUser;
  }

    // LOGIN
    public void correctPassword (User user, String password){
        if (!user.getPassword().equals(password)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is wrong. Check the spelling");
        }
    }

    public User getUserByUsername(String username){
        User UserIGetPerUsername = userRepository.findByUsername(username);
        if (UserIGetPerUsername == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"There exists no user with this username.");
        } return UserIGetPerUsername;
    }

    public void login(User user){
        user.setStatus(UserStatus.ONLINE);
        user = userRepository.save(user);
        userRepository.flush();
    }




  /**
  * This is a helper method that will check the uniqueness criteria of the username
  * defined in the User entity. The method will do nothing if the input is unique
  * and throw an error (CONFLICT, 409) otherwise.
  */
  private void checkIfUsernameExists(String username) {
    User userByUsername = userRepository.findByUsername(username);

    if (userByUsername != null) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("The username %s is already taken!", username));
    }
  }




}
