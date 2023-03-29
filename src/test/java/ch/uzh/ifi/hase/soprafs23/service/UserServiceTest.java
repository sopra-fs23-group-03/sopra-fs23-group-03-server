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

public class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserService userService;

  private User user;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);

    // given
    user = new User();
    user.setId(1L);
    user.setUsername("fisrtUsername");
    user.setPassword("firstPassword");

    // mocks the save() method of UserRepository
    Mockito.when(userRepository.save(Mockito.any())).thenReturn(user);
  }

  @Test
  public void createUser_validInputs_success() {
    User createdUser = userService.createUser(user);

    // then
    verify(userRepository, times(1)).save(Mockito.any());
    verify(userRepository, times(1)).flush();

    assertNotNull(createdUser.getId());
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

}
