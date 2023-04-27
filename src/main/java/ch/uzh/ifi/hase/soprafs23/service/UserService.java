package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserPutDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Optional;
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

  public List getUsersFreeToInvite(){
      // TODO: List with users which are not in a group yet
      List L = new ArrayList<>();
    return L;
  }

  // returns the userid of the user with the given token
  // returns 0L if no user has the given token
  public Long getUseridByToken(String token) {
    if(token == null) {
      return 0L;
    }

    List<User> users = userRepository.findByToken(token);
    assert users.size() <= 1; // if this is not the case, there is duplicate tokens!!!

    if(users.size() == 1) {
      return users.get(0).getId();
    }

    return 0L; 
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
    public User getUserById(Long id) {
        Optional<User> user = this.userRepository.findById(id);

        if(!user.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("User with id %s does not exist.", id));
        }

        return user.get();
    }

    // LOGIN
    public void correctPassword(String username, String password) {
        User user = getUserByUsername(username);
        if (!user.getPassword().equals(password)) {
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
        user.setToken(UUID.randomUUID().toString()); // set new token upon login
        user.setStatus(UserStatus.ONLINE);
        user = userRepository.save(user);
        userRepository.flush();
    }
    public void logout(Long id) {
        User user = getUserById(id);
        user.setStatus(UserStatus.OFFLINE);
        user = userRepository.save(user);
        userRepository.flush();
    }

    public void updateUser(Long id, UserPutDTO userPutDTO) {
        User user = getUserById(id);

        String newUsername = userPutDTO.getUsername();
        Set<String> newAllergies = userPutDTO.getAllergies();
        Set<String> newFavoriteCuisine = userPutDTO.getFavoriteCuisine();
        String newSpecialDiet = userPutDTO.getSpecialDiet();
        String newPassword = userPutDTO.getPassword();
        String currentPassword = userPutDTO.getCurrentPassword();

        if(!isPasswordCorrect(user.getPassword(), currentPassword)) {
          throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The current password is incorrect");
        }

        if(newPassword != null && !newPassword.isEmpty()){
          if(!user.getPassword().equals(newPassword)){
              user.setPassword(newPassword);
          } else {
              throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password cannot be the same as the current password.");
          }
          
      } else if (newPassword != null) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password cannot be empty.");
      }

        if(newUsername != null) {
            checkIfUsernameExists(newUsername);
            user.setUsername(newUsername);
        }

        if(newAllergies != null) {
            for(String allergy : newAllergies) {
              user.addAllergy(allergy);
            }
        }

        if(newFavoriteCuisine != null){
          for(String cuisine : newFavoriteCuisine) {
            user.addFavouriteCuisine(cuisine);
          }
        }

        if(newSpecialDiet != null){
            user.setSpecialDiet(newSpecialDiet);
        }



        user = userRepository.save(user);
        userRepository.flush();
    }

    public boolean isPasswordCorrect(String storedPassword, String providedPassword) {
        return storedPassword.equals(providedPassword);
    }


    public void joinGroup(Long guestId, Long groupId) {
        User guest = getUserById(guestId);
        if(guest.getGroupId() != null) {
          throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("User with id %s is already in the group with id %d.", guest.getId(), guest.getGroupId()));
        }
        
        guest.setGroupId(groupId);
        
        guest = userRepository.save(guest);
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
