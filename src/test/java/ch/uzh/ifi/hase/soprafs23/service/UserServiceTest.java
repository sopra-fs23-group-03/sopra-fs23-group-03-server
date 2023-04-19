package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

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
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(user);
    }

    @Test
    public void getUseridByToken_test() {
        // mocks the findByToken(String token) method of UserRepository
        List<User> listWithTheUser = new ArrayList<User>();
        listWithTheUser.add(user);
        Mockito.when(userRepository.findByToken(user.getToken())).thenReturn(listWithTheUser);
        List<User> listWithNoUser = new ArrayList<User>();
        Mockito.when(userRepository.findByToken("newToken")).thenReturn(listWithNoUser);


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
        Mockito.when(userRepository.findByUsername(user.getUsername())).thenReturn(user);

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
        Mockito.when(userRepository.findByUsername("new")).thenReturn(null);

        assertThrows(ResponseStatusException.class, () -> userService.getUserByUsername("new"));
    }

    @Test
    public void testCorrectPassword() {
        user.setUsername("testuser");
        user.setPassword("testpassword");
        Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(user);

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
}
