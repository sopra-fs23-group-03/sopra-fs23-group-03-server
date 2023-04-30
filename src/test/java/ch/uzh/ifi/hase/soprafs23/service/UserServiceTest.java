package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserPutDTO;
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

    @InjectMocks
    private UserService userService;
    private UserPutDTO userPutDTO;

    private User user;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // given
        user = new User();
        user.setId(1L);
        user.setUsername("fisrtUsername");
        user.setPassword("firstPassword");
        user.setToken("firstToken");

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

        Set<String> expectedAllergies = new HashSet<>();
        expectedAllergies.addAll(existingUser.getAllergiesSet());
        expectedAllergies.addAll(userPutDTO.getAllergies());
        assertEquals(expectedAllergies, existingUser.getAllergiesSet());

        Set<String> expectedCuisines = new HashSet<>();
        expectedCuisines.addAll(existingUser.getFavoriteCuisineSet());
        expectedCuisines.addAll(userPutDTO.getFavoriteCuisine());
        assertEquals(expectedCuisines, existingUser.getFavoriteCuisineSet());

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
}
