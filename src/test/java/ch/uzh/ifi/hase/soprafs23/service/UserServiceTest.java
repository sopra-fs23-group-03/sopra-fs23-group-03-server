package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.entity.Ingredient;
import ch.uzh.ifi.hase.soprafs23.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs23.repository.IngredientRepository;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.IngredientPutDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupService groupService;

    @Mock
    private IngredientRepository ingredientRepository;

    @InjectMocks
    private UserService userService;

    private UserPutDTO userPutDTO;

    private IngredientPutDTO ingredientPutDTO;

    private User user;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // given
        user = new User();
        user.setId(1L);
        user.setUsername("firstUsername");
        user.setPassword("firstPassword");
        user.setToken("firstToken");

        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(new Ingredient("ingredient1"));
        ingredients.add(new Ingredient("ingredient2"));
        user.addIngredient(ingredients);

        // mocks the save() method of UserRepository
        when(userRepository.save(Mockito.any())).thenReturn(user);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    }

    @Test
    public void getUseridByToken_test() {
        // mocks the findByToken(String token) method of UserRepository
        List<User> listWithTheUser = new ArrayList<User>();
        listWithTheUser.add(user);
        when(userRepository.findByToken(user.getToken())).thenReturn(listWithTheUser);

        List<User> listWithNoUser = new ArrayList<User>();
        when(userRepository.findByToken("anotherToken")).thenReturn(listWithNoUser);

        assertEquals(1L, userService.getUseridByToken(user.getToken()));
        assertEquals(0L, userService.getUseridByToken("anotherToken"));
    }

    @Test
    public void createUser_validInputs_success() {
        User createdUser = userService.createUser(user);

        // then
        verify(userRepository, times(1)).save(Mockito.any());
        verify(userRepository, times(1)).flush();

        assertEquals(createdUser.getId(), user.getId());
        assertEquals(createdUser.getUsername(), user.getUsername());
        assertEquals(createdUser.getPassword(), user.getPassword());
        assertNotNull(createdUser.getToken());
        assertEquals(createdUser.getStatus(), UserStatus.ONLINE);
    }

    @Test
    public void createUser_duplicateUsername_throwsException() {
        // a first user has already been created
        userService.createUser(user);

        // mocks findByUsername(username) method in UserRepository
        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);

        // then
        assertThrows(ResponseStatusException.class, () -> userService.createUser(user));
    }

    @Test
    public void createUser_invalidUsername_throwsException() {
        user.setUsername("invalid-username");

        assertThrows(ResponseStatusException.class, () -> userService.createUser(user));
    }

    @Test
    public void createUser_equalUsernameAndPassword_throwsException() {
        user.setUsername("word");
        user.setPassword("word");

        assertThrows(ResponseStatusException.class, () -> userService.createUser(user));
    }

    @Test
    public void loginUser_NameNotExists_throwsException() {
        // mocks findByUsername(username) method in UserRepository
        when(userRepository.findByUsername("new")).thenReturn(null);

        assertThrows(ResponseStatusException.class, () -> userService.getUserByUsername("new"));
    }

    @Test
    public void testCorrectPassword() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);

        // Test correct password
        userService.correctPassword(user.getUsername(), user.getPassword());

        // Test incorrect password
        assertThrows(ResponseStatusException.class, () -> userService.correctPassword(user.getUsername(), "incorrectpassword"));
    }

    @Test
    public void loginUser_validInputs_success() {
        userService.createUser(user);
        user.setStatus(UserStatus.OFFLINE);
        userService.login(user);
        assertEquals(user.getStatus(), UserStatus.ONLINE);
        assertNotEquals("firstToken", user.getToken());
    }

    @Test
    void updateUser_ShouldUpdateUserFields_WhenUserExists() {
        // given
        Long id = 5L;
        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("newUsername");
        userPutDTO.setAllergies(Collections.singleton("newAllergies"));
        userPutDTO.setFavoriteCuisine(Collections.singleton("Swiss"));
        userPutDTO.setSpecialDiet("newSpecialDiet");
        userPutDTO.setPassword("newPassword");
        userPutDTO.setCurrentPassword("oldPassword");

        User existingUser = new User();
        existingUser.setId(id);
        existingUser.setUsername("oldUsername");
        existingUser.addAllergy("oldAllergy");
        existingUser.addFavouriteCuisine("oldFavoriteCuisine");
        existingUser.setSpecialDiet("oldSpecialDiet");
        existingUser.setPassword("oldPassword");

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));

        // when
        userService.updateUser(id, userPutDTO);

        // then
        assertEquals(userPutDTO.getUsername(), existingUser.getUsername());
        assertEquals(userPutDTO.getAllergies(), existingUser.getAllergiesSet());
        assertEquals(userPutDTO.getFavoriteCuisine(), existingUser.getFavoriteCuisineSet());
        assertEquals(userPutDTO.getSpecialDiet(), existingUser.getSpecialDiet());
        assertEquals(userPutDTO.getPassword(), existingUser.getPassword());
    }

    @Test
    public void testUpdateUserWithInvalidId() {
        Long invalidId = 123456L;
        UserPutDTO userPutDTO = new UserPutDTO();

        when(userRepository.findById(invalidId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> userService.updateUser(invalidId, userPutDTO));
    }

    @Test
    public void testUpdateUserWithExistingUsername() {
        String newUsername = "new_username";

        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername(newUsername);

        when(userRepository.findByUsername(newUsername)).thenReturn(new User());

        assertThrows(ResponseStatusException.class, () -> userService.updateUser(user.getId(), userPutDTO));
    }

    @Test
    void updateUser_withSamePassword_throwsException() {
        userPutDTO = new UserPutDTO();
        userPutDTO.setCurrentPassword(user.getPassword());
        userPutDTO.setPassword(user.getPassword());

        assertThrows(ResponseStatusException.class, () -> userService.updateUser(user.getId(), userPutDTO));
    }

    @Test
    void updateUser_withEmptyPassword_throwsException() {
        userPutDTO = new UserPutDTO();
        userPutDTO.setCurrentPassword(user.getPassword());
        userPutDTO.setPassword("");
        
        assertThrows(ResponseStatusException.class, () -> userService.updateUser(user.getId(), userPutDTO));
    }

    @Test
    void updateUser_withIncorrectCurrentPassword_throwsException() {
        userPutDTO = new UserPutDTO();
        userPutDTO.setPassword("new password");
        userPutDTO.setCurrentPassword("incorrect password");

        assertThrows(ResponseStatusException.class, () -> userService.updateUser(user.getId(), userPutDTO));
    }

    @Test
    void addUserIngredients_withExistingUserIdAndNewIngredient_addsIngredientToUser() {
        // user needs to be in a group
        user.setGroupId(5L);
        when(groupService.getGroupById(user.getGroupId())).thenReturn(new Group());

        Long userId = user.getId();
        IngredientPutDTO ingredientPutDTO = new IngredientPutDTO();
        ingredientPutDTO.setName("new_ingredient_name");

        Ingredient ingredient = new Ingredient("new_ingredient_name");
        when(ingredientRepository.findByName(ingredientPutDTO.getName())).thenReturn(Optional.of(ingredient));

        userService.addUserIngredients(userId, Collections.singletonList(ingredientPutDTO));

        verify(userRepository).save(user);
        assertEquals(3, user.getIngredients().size()); //expects 3 bc in the setup are already 2
        assertTrue(user.getIngredients().contains(ingredient));
    }

    @Test
    void addUserIngredients_withExistingIngredients_doesNotAddDuplicatesToUser() {
        // user needs to be in a group
        user.setGroupId(5L);
        when(groupService.getGroupById(user.getGroupId())).thenReturn(new Group());

        Long userId = user.getId();

        Ingredient existingIngredient = user.getIngredients().iterator().next();
        IngredientPutDTO ingredientPutDTO = new IngredientPutDTO();
        ingredientPutDTO.setName(existingIngredient.getName()); //get another ingredient with the same name

        when(ingredientRepository.findByName(existingIngredient.getName())).thenReturn(Optional.of(existingIngredient));

        userService.addUserIngredients(userId, Collections.singletonList(ingredientPutDTO));

        verify(userRepository).save(user);
        assertEquals(user.getIngredients().size(), 2); // bc 2 are mentioned above in setup
    }


    @Test
    void addUserIngredients_withNonexistentUserId_throwsException() {
        Long nonexistentUserId = 999L;
        IngredientPutDTO ingredientPutDTO = new IngredientPutDTO();
        ingredientPutDTO.setName("new_ingredient_name");

        when(userRepository.findById(nonexistentUserId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> userService.addUserIngredients(nonexistentUserId, Collections.singletonList(ingredientPutDTO)));
    }



}
