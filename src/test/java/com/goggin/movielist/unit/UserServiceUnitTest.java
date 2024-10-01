package com.goggin.movielist.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.goggin.movielist.exception.NoLoggedInUserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.goggin.movielist.exception.UsernameAlreadyExistsException;
import com.goggin.movielist.model.User;
import com.goggin.movielist.respositories.UserRepository;
import com.goggin.movielist.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks // creates an instance of the class, with the mocks injected into it
    private UserService userService;

    @BeforeEach
    void setUp() {
        User mockUser = new User(1, "jackhenryg@hotmail.co.uk", "test", "pwd", 51.5074, -0.1278);

        Authentication authentication = new UsernamePasswordAuthenticationToken(mockUser.getUsername(),
                mockUser.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userRepository.findByUsername(mockUser.getUsername())).thenReturn(Optional.of(mockUser));
    }

    @Test
    void shouldSuccessfullyChangeUsername() throws UsernameAlreadyExistsException, NoLoggedInUserException {
        // arrange
        User updatedUser = new User();
        updatedUser.setUsername("valid_unused_username");
        when(userRepository.existsByUsername(anyString())).thenReturn(false); // mock username not existing already

        // acts
        userService.updateUser(updatedUser);

        System.out.println("2893509q285093579012759072590");
        log.info("User = {}", userRepository.findById(1));

        // assert
        verify(userRepository).save(argThat(user -> "valid_unused_username".equals(user.getUsername())));
        Authentication newAuth = SecurityContextHolder.getContext().getAuthentication();
        assertEquals(updatedUser.getUsername(), newAuth.getName()); // verify the authenticaiton was updated
    }

    @Test
    void shouldThrowUsernameAlreadyExistsExceptionBecauseThatIsUsersExistingName() {
        // arrange
        User updatedUser = new User();
        updatedUser.setUsername("test");

        when(userRepository.existsByUsername(anyString())).thenReturn(false);

        // act and assert
        Exception exception = assertThrows(UsernameAlreadyExistsException.class,
                () -> userService.updateUser(updatedUser));
        assertEquals("You cannot change to your existing username!", exception.getMessage());
    }

    @Test
    void shouldThrowUsernameAlreadyExistsExceptionBecauseSomebodyElseUsedIt() {
        // arrange
        User updatedUser = new User();
        updatedUser.setUsername("someone_else's_username");
        when(userRepository.existsByUsername(anyString())).thenReturn(true); // mock the username match

        // act and assert
        Exception exception = assertThrows(UsernameAlreadyExistsException.class,
                () -> userService.updateUser(updatedUser));
        assertEquals("New username is taken by somebody else!", exception.getMessage());

    }
}
