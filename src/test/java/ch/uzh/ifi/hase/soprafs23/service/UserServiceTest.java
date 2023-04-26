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
import org.springframework.http.HttpStatus;
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

    private User user; // so we will always take user as testuser

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
    }

    @Test
    public void getUseridByToken_test() {
        // mocks the findByToken(String token) method of UserRepository
        List<User> listWithTheUser = new ArrayList<User>();
        listWithTheUser.add(user);
        when(userRepository.findByToken(user.getToken())).thenReturn(listWithTheUser);
        List<User> listWithNoUser = new ArrayList<User>();
        when(userRepository.findByToken("newToken")).thenReturn(listWithNoUser);


        assertEquals(1L, userService.getUseridByToken(user.getToken()));
        assertEquals(0L, userService.getUseridByToken("newToken"));

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
        user.setUsername("testuser");
        user.setPassword("testpassword");
        when(userRepository.findByUsername(Mockito.any())).thenReturn(user);

        // Test correct password
        userService.correctPassword("testuser", "testpassword");

        // Test incorrect password
        try {
            userService.correctPassword("testuser", "incorrectpassword");
            fail("Expected ResponseStatusException was not thrown");
        } catch (ResponseStatusException e) {
            assertEquals("Password is wrong. Check the spelling", e.getReason());
        }
    }


    @Test
    public void loginUser_validInputs_success() {
        userService.createUser(user);
        user.setStatus(UserStatus.OFFLINE);
        userService.login(user); // needs to be o set to online then
        assertEquals(user.getStatus(), UserStatus.ONLINE);
        assertNotEquals("firstToken", user.getToken());

    }

    @Test
    void updateUser_ShouldUpdateUserFields_WhenUserExists() {
        // given
        Long id = 1L;
        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("newUsername");
        userPutDTO.setAllergies(Collections.singleton("newAllergies"));
        userPutDTO.setFavoriteCuisine("newFavoriteCuisine");
        userPutDTO.setSpecialDiet("newSpecialDiet");
        userPutDTO.setPassword("newPassword");

        User existingUser = new User();
        existingUser.setId(id);
        existingUser.setUsername("oldUsername");
        existingUser.addAllergy("oldAllergy");
        existingUser.setFavoriteCuisine("oldFavoriteCuisine");
        existingUser.setSpecialDiet("oldSpecialDiet");
        existingUser.setPassword("oldPassword");

        when(userRepository.findById(id)).thenReturn(java.util.Optional.of(existingUser));

        // when
        userService.updateUser(id, userPutDTO,"oldPassword");

        // then
        assertEquals(userPutDTO.getUsername(), existingUser.getUsername());
        assertEquals(userPutDTO.getAllergies(), existingUser.getAllergiesSet());
        assertEquals(userPutDTO.getFavoriteCuisine(), existingUser.getFavoriteCuisine());
        assertEquals(userPutDTO.getSpecialDiet(), existingUser.getSpecialDiet());
        assertEquals(userPutDTO.getPassword(), existingUser.getPassword());
    }

    @Test
    public void testUpdateUserWithInvalidId() {
        Long invalidId = 123456L;
        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("new_username");

        when(userRepository.findById(invalidId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            userService.updateUser(invalidId, userPutDTO,"oldPassword");
        });
    }

    @Test
    public void testUpdateUserWithExistingUsername() {
        Long userId = 1L;
        String existingUsername = "existing_username";
        String newUsername = "new_username";
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setUsername(existingUsername);
        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername(newUsername);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername(newUsername)).thenReturn(new User());

        assertThrows(ResponseStatusException.class, () -> {
            userService.updateUser(userId, userPutDTO,"oldPassword");
        });
    }

    @Test
    void updateUser_withSamePassword_throwsException() {
        user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setPassword("currentPassword");
        user.setAllergiesSet(Collections.singleton("testAllergies"));
        user.setFavoriteCuisine("testFavoriteCuisine");
        user.setSpecialDiet("testSpecialDiet");

        userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("newTestUser");
        userPutDTO.setAllergies(Collections.singleton("newTestAllergies"));
        userPutDTO.setFavoriteCuisine("newTestFavoriteCuisine");
        userPutDTO.setSpecialDiet("newTestSpecialDiet");
        
        // Arrange
        userPutDTO.setPassword("currentPassword");
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));

        // Act
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.updateUser(1L, userPutDTO, user.getPassword()));

        // Assert
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("New password cannot be the same as the current password.", exception.getReason());
    }

    @Test
    void updateUser_withEmptyPassword_throwsException() {
        user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setPassword("currentPassword");
        user.setAllergiesSet(Collections.singleton("testAllergies"));
        user.setFavoriteCuisine("testFavoriteCuisine");
        user.setSpecialDiet("testSpecialDiet");

        userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("newTestUser");
        userPutDTO.setAllergies(Collections.singleton("newTestAllergies"));
        userPutDTO.setFavoriteCuisine("newTestFavoriteCuisine");
        userPutDTO.setSpecialDiet("newTestSpecialDiet");

        // Arrange
        userPutDTO.setPassword("");
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));

        // Act
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.updateUser(1L, userPutDTO,"currentPassword"));

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("New password cannot be empty.", exception.getReason());
    }



    @Test
    void updateUser_withIncorrectCurrentPassword_throwsException() {
        user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setPassword("currentPassword");
        user.setAllergiesSet(Collections.singleton("testAllergies"));
        user.setFavoriteCuisine("testFavoriteCuisine");
        user.setSpecialDiet("testSpecialDiet");

        userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("newTestUser");
        userPutDTO.setAllergies(Collections.singleton("newTestAllergies"));
        userPutDTO.setFavoriteCuisine("newTestFavoriteCuisine");
        userPutDTO.setSpecialDiet("newTestSpecialDiet");
        userPutDTO.setPassword("newPassword");

        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));


        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.updateUser(1L, userPutDTO, "incorrectPassword"));


        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("Current password is incorrect.", exception.getReason());
    }
}
