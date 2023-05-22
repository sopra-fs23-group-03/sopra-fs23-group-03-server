package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;

import ch.uzh.ifi.hase.soprafs23.entity.*;

import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.entity.Ingredient;
import ch.uzh.ifi.hase.soprafs23.constant.UserVotingStatus;
import ch.uzh.ifi.hase.soprafs23.repository.FullIngredientRepository;
import ch.uzh.ifi.hase.soprafs23.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs23.repository.IngredientRepository;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.IngredientPutDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


import java.util.*;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final GroupService groupService;
    @Autowired
    private final IngredientRepository ingredientRepository;
    @Autowired
    private FullIngredientRepository fullIngredientRepository;

    @Autowired
    public UserService(@Qualifier("userRepository") UserRepository userRepository, IngredientRepository ingredientRepository, @Lazy GroupService groupService, FullIngredientRepository fullIngredientRepository) {
        this.userRepository = userRepository;
        this.ingredientRepository = ingredientRepository;
        this.groupService =  groupService;
        this.fullIngredientRepository = fullIngredientRepository;
    }

    public List<User> getUsers() {
        return this.userRepository.findAll();
    }

    // returns the userId of the user with the given token
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

    public User createUser(User newUser) {
        newUser.setToken(UUID.randomUUID().toString());
        newUser.setStatus(UserStatus.ONLINE);
        newUser.setVotingStatus(UserVotingStatus.NOT_VOTED);
        newUser.setSpecialDiet("omnivore");
      
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
        if(isUserInGroup(id)){
            leaveGroup(id);
        }
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

        if(newPassword != null && currentPassword != null) {
            if(!isPasswordCorrect(user.getPassword(), currentPassword)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The current password is incorrect");
            } else if(newPassword.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password cannot be empty.");
            } else if(user.getPassword().equals(newPassword)){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password cannot be the same as the current password.");
            }
            
            user.setPassword(newPassword);            
        } else if(newPassword != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "To change your password please enter your current password");
        }

        if(newUsername != null && !newUsername.equals(user.getUsername())) {
            checkIfUsernameExists(newUsername);
            // check if username is only latin letters
            if (!newUsername.matches("^[a-zA-Z]+$")) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "The username should only contain Latin letters (a-z, A-Z)!");
            }
            // check that username and password are not the same
            if(newUsername.equals(user.getPassword())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "The username and password cannot be the same!");
            }
            user.setUsername(newUsername);
        }

        user.removeAllergies();
        if(newAllergies != null) {
            for(String allergy : newAllergies) {
                user.addAllergy(allergy);
            }
        }

        user.removeFavouriteCuisines();
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

    private void checkIfUsernameExists(String username) {
        User userByUsername = userRepository.findByUsername(username);

        if (userByUsername != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("The username %s is already taken!", username));
        }
    }

    public synchronized void addUserIngredients(Long userId, List<IngredientPutDTO> ingredientsPutDTO) {
        User user = getUserById(userId);

        Set<Ingredient> newIngredients = new HashSet<>(); // Changed from List to Set
        List<String> missingIngredients = new ArrayList<>();

        if(user.getGroupId() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You must be part of a group to add ingredients.");
        }

        Group group = groupService.getGroupById(user.getGroupId());

        for (IngredientPutDTO ingredientPutDTO : ingredientsPutDTO) {
            Optional<FullIngredient> fullIngredientOptional = fullIngredientRepository.findByName(ingredientPutDTO.getName());

            if (!fullIngredientOptional.isPresent()) {
                missingIngredients.add(ingredientPutDTO.getName());
                continue;
            }

            Optional<Ingredient> ingredientOptional = ingredientRepository.findByNameAndGroupId(ingredientPutDTO.getName(), user.getGroupId());
            Ingredient ingredient;
            if(ingredientOptional.isPresent()) {
                ingredient = ingredientOptional.get();
            } else {
                ingredient = new Ingredient(ingredientPutDTO.getName());
            }

            if(ingredient.getGroup() != null && !ingredient.getGroup().getId().equals(group.getId())) {
                ingredient = new Ingredient(ingredientPutDTO.getName());
            }

            // Declare a separate variable for the ingredient name
            String ingredientName = ingredient.getName();

            // Check if the user already has an ingredient with the same name
            boolean userAlreadyHasIngredient = user.getIngredients().stream()
                    .anyMatch(existingIngredient -> existingIngredient.getName().equals(ingredientName));

            boolean newIngredientsContainsIngredient = newIngredients.stream()
                    .anyMatch(existingIngredient -> existingIngredient.getName().equals(ingredientName));

            if (!userAlreadyHasIngredient && !newIngredientsContainsIngredient) {
                ingredient.setGroup(group);
                ingredient.getUsersSet().add(user);
                newIngredients.add(ingredient);
            }
        }

        if (!missingIngredients.isEmpty()) {
            String missingIngredientsString = String.join(", ", missingIngredients);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No ingredient with the name " + missingIngredientsString + " found in the full ingredient list.");
        }
        user.getIngredients().addAll(newIngredients);
        userRepository.save(user);
    }




    @Transactional //for Spring; makes all changes to db persisted in one single transaction --> helps rolling back in case of an error (data consistency)
    public void updateIngredientRatings(Long groupId, Long userId, Map <Long, String> ingredientRatings) {
        // fetch User and Group
        User user = getUserById(userId); // 404 - user not found
        Group group = groupService.getGroupById(groupId);  // 404 - group not found

        if(user.getVotingStatus() == UserVotingStatus.VOTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You voted already"); // 409 - user already voted
            // TODO: check (or change) enum everytime someone leaves the group, also at the end after planning when redirected to Landing Page
        }

        // Count entries in map and check if user rated all of them.
        int countGivenRatings = ingredientRatings.size();

        Set<Ingredient> groupIngredients = group.getIngredients();
        int countNeededRatings = groupIngredients.size();

        if (countGivenRatings != countNeededRatings) { // 422 - not enough ratings send
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, countGivenRatings + " ratings given, but " + countNeededRatings + " ratings needed. Amount of ratings not matching.");
        }

        // for each ingredient in ingredientRatings
        for (Map.Entry<Long, String> entry : ingredientRatings.entrySet()) {
            Long ingredientId = entry.getKey();
            String userRating = entry.getValue();

            // find Ingredient based on IngredientID
            Ingredient ingredient = group.getIngredients()
                    .stream()
                    .filter(i -> i.getId().equals(ingredientId))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ingredient with given ID " + ingredientId + " not found"));

            // add rating to singleUserRatings list of found Ingredient for the given User
            List<String> singleUserRatings = ingredient.getSingleUserRatings();
            singleUserRatings.add(userRating);
            ingredient.setSingleUserRatings(singleUserRatings);

            ingredientRepository.save(ingredient);
        }

        ingredientRepository.flush();
        user.setVotingStatus(UserVotingStatus.VOTED);
    }

    public void leaveGroup(Long userId) {
        User user = getUserById(userId);
        Long groupId = user.getGroupId();

        if (groupId != null) {
            Group group = groupService.getGroupById(groupId);
            groupService.removeGuestFromGroup(group, userId);
        }

        user.setVotingStatus(UserVotingStatus.NOT_VOTED);
        user.setGroupId(null);
        userRepository.save(user);
        userRepository.flush();
    }

    public boolean isUserInGroup(Long userId) {
        User user = getUserById(userId);
        return user.getGroupId() != null;
    }

    public Set<String> getAllergiesById(Long userId) {
        User user = getUserById(userId);
        return user.getAllergiesSet();
    }

    public boolean userHasIngredients(Long userId) {
        User user = getUserById(userId);
        int ingredientSetSize = user.getIngredients().size();
        return ingredientSetSize > 0;
    }

    public void setUserReady(Long userId) {
        User user = this.getUserById(userId);
        user.setReady(true);
        userRepository.save(user);
    }

    public boolean areAllUsersReady(List<Long> userIds) {
        return userIds.stream()
                .map(this::getUserById)
                .allMatch(User::isReady);
    }

    public void setAllUsersNotReady(List<Long> userIds) {
        userIds.forEach(id -> {
            User member = getUserById(id);
            member.setReady(false);
            userRepository.save(member);
        });
    }

    public void deleteGroupAndSetAllUsersNotReady(Long groupId, List<Long> userIds) {
        groupService.deleteGroup(groupId);
        setAllUsersNotReady(userIds);
    }

    public Map<Long, Boolean> getGroupUserReadyStatus(Long groupId) {

        //404 - group not found
        Group group = groupService.getGroupById(groupId);

        List<Long> memberIds = groupService.getAllMemberIdsOfGroup(group);

        Map<Long, Boolean> userReadyStatus = new HashMap<>();
        for (Long memberId : memberIds) {
            User user = this.getUserById(memberId);
            if (user != null) {
                userReadyStatus.put(memberId, user.isReady());
            }
        }
        return userReadyStatus;
    }
}