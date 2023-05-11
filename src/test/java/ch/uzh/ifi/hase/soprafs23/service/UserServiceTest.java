package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.entity.FullIngredient;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.entity.Ingredient;
import ch.uzh.ifi.hase.soprafs23.constant.UserVotingStatus;
import ch.uzh.ifi.hase.soprafs23.repository.FullIngredientRepository;
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

    @Mock
    private FullIngredientRepository fullIngredientRepository;

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
        user.setGroupId(2L);
        user.setVotingStatus(UserVotingStatus.NOT_VOTED);

        // to add to the users
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
    public void addUserIngredients_withExistingUserIdAndNewIngredient_addsIngredientToUser() {
        // given
        ingredientPutDTO = new IngredientPutDTO();
        ingredientPutDTO.setName("newIngredient");

        FullIngredient fullIngredient = new FullIngredient("newIngredient");
        when(fullIngredientRepository.findByName("newIngredient")).thenReturn(Optional.of(fullIngredient));
        when(ingredientRepository.findByName("newIngredient")).thenReturn(Optional.empty());

        Group group = new Group();
        group.setId(2L);
        when(groupService.getGroupById(user.getGroupId())).thenReturn(group);

        // when
        userService.addUserIngredients(user.getId(), Collections.singletonList(ingredientPutDTO));

        // then
        assertTrue(user.getIngredients().stream().anyMatch(ingredient -> ingredient.getName().equals("newIngredient")));
    }

    @Test
    public void addUserIngredients_withExistingIngredients_doesNotAddDuplicatesToUser() {
        // given
        ingredientPutDTO = new IngredientPutDTO();
        ingredientPutDTO.setName("ingredient1");

        FullIngredient fullIngredient = new FullIngredient("ingredient1");
        when(fullIngredientRepository.findByName("ingredient1")).thenReturn(Optional.of(fullIngredient));

        Ingredient existingIngredient = new Ingredient("ingredient1");
        Group existingGroup = new Group();
        existingGroup.setId(2L);
        existingIngredient.setGroup(existingGroup);
        when(ingredientRepository.findByName("ingredient1")).thenReturn(Optional.of(existingIngredient));

        Group group = new Group();
        group.setId(2L);
        when(groupService.getGroupById(user.getGroupId())).thenReturn(group);

        int initialIngredientCount = user.getIngredients().size();

        // when
        userService.addUserIngredients(user.getId(), Collections.singletonList(ingredientPutDTO));

        // then
        assertEquals(initialIngredientCount, user.getIngredients().size());

        // Additional verification
        long ingredient1Count = user.getIngredients().stream()
                .filter(ingredient -> ingredient.getName().equals("ingredient1"))
                .count();
        assertEquals(1, ingredient1Count);
    }


    @Test
    void addUserIngredients_withNonexistentUserId_throwsException() {
        Long nonexistentUserId = 999L;
        IngredientPutDTO ingredientPutDTO = new IngredientPutDTO();
        ingredientPutDTO.setName("new_ingredient_name");

        when(userRepository.findById(nonexistentUserId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> userService.addUserIngredients(nonexistentUserId, Collections.singletonList(ingredientPutDTO)));
    }

    @Test
    void updateIngredientRatings_whenAllIngredientsRated_updatesUserVotingStatus() {
        // given
        Map<Long, String> ingredientRatings = new HashMap<>();
        ingredientRatings.put(1L, "1");
        ingredientRatings.put(2L, "-1");
        User user = new User();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // to add to the group
        Ingredient ingredient1 = new Ingredient("ingredient12");
        ingredient1.setId(1L);
        Ingredient ingredient2 = new Ingredient("ingredient23");
        ingredient2.setId(2L);
        Set<Ingredient> ingredientsSet = new HashSet<>();
        ingredientsSet.add(ingredient1);
        ingredientsSet.add(ingredient2);

        Group group = new Group();
        group.addIngredient(new ArrayList<>(ingredientsSet));

        when(groupService.getGroupById(group.getId())).thenReturn(group);

        // when
        userService.updateIngredientRatings(group.getId(), user.getId(), ingredientRatings);

        // then
        assertEquals(UserVotingStatus.VOTED, user.getVotingStatus());
    }

    @Test
    void updateIngredientRatings_withNonexistentUser_throwsException() {
        // given
        Long groupId = 1L;
        Long nonexistentUserId = 999L;
        Map<Long, String> ingredientRatings = new HashMap<>();

        when(userRepository.findById(nonexistentUserId)).thenReturn(Optional.empty());

        // then
        assertThrows(ResponseStatusException.class, () -> userService.updateIngredientRatings(groupId, nonexistentUserId, ingredientRatings),
                "User with given ID " + nonexistentUserId + " not found");
        verify(groupService, never()).getGroupById(anyLong());
    }

    @Test
    void updateIngredientRatings_whenUserAlreadyVoted_throwsException() {
        // given
        Long groupId = 1L;
        Map<Long, String> ingredientRatings = new HashMap<>();

        User user = new User();
        user.setVotingStatus(UserVotingStatus.VOTED);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        Group group = new Group();
        when(groupService.getGroupById(groupId)).thenReturn(group);

        // then
        assertThrows(ResponseStatusException.class, () -> userService.updateIngredientRatings(groupId, user.getId(), ingredientRatings),
                "You voted already");
    }

    @Test
    void updateIngredientRatings_withInsufficientRatings_throwsException() {
        // given
        Long groupId = 1L;
        Map<Long, String> ingredientRatings = new HashMap<>();
        ingredientRatings.put(1L, "-1");

        Group group = new Group();
        group.setId(groupId);

        // to add to the group
        Ingredient ingredient1 = new Ingredient("ingredient1");
        ingredient1.setId(1L);
        Ingredient ingredient2 = new Ingredient("ingredient2");
        ingredient2.setId(2L);

        Set<Ingredient> ingredientsSet = new HashSet<>();
        ingredientsSet.add(ingredient1);
        ingredientsSet.add(ingredient2);

        group.addIngredient(new ArrayList<>(ingredientsSet));

        User user = new User();
        user.setVotingStatus(UserVotingStatus.NOT_VOTED);

        // mock necessary repositories and services
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(groupService.getGroupById(groupId)).thenReturn(group);

        // when, then
        assertThrows(ResponseStatusException.class, () -> userService.updateIngredientRatings(groupId, user.getId(), ingredientRatings));
        verify(ingredientRepository, never()).save(any());
        assertEquals(UserVotingStatus.NOT_VOTED, user.getVotingStatus()); // important to check, so user could vote again
    }

    @Test
    void leaveGroup_test() {
        userService.leaveGroup(user.getId());
        assertEquals(null, user.getGroupId());
    }

}
