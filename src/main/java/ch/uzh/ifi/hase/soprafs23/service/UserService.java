package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private final Logger log = LoggerFactory.getLogger(UserService.class);

  private final UserRepository userRepository;

  @Autowired
  public UserService(@Qualifier("userRepository") UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public List<User> getUsers() {
    return this.userRepository.findAll();
  }

  public User createUser(User newUser) {
    newUser.setToken(UUID.randomUUID().toString());
    newUser.setStatus(UserStatus.ONLINE);

    // check that the username is still free
    checkIfUsernameExists(newUser.getUsername());

    // saves the given entity but data is only persisted
    // in the database once flush() is called
    newUser = userRepository.save(newUser);
    userRepository.flush();

    return newUser;
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


  // LOGIN
  public void correctPassword (User user, String password){
      if (!user.getPassword().equals(password)){
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is wrong. Check spelling");
      }
  }

}
