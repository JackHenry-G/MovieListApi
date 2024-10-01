package com.goggin.movielist.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.goggin.movielist.exception.NoLoggedInUserException;
import com.goggin.movielist.model.MovieConnection;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;

import com.goggin.movielist.exception.UsernameAlreadyExistsException;
import com.goggin.movielist.model.User;
import com.goggin.movielist.respositories.UserRepository;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserService implements UserDetailsService {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserRepository userRepository;
    private final MovieService movieService;

    public UserService(final BCryptPasswordEncoder bCryptPasswordEncoder, final UserRepository userRepository, final MovieService movieService) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userRepository = userRepository;
        this.movieService = movieService;
    }

    public void saveNewUser(User user) throws UsernameAlreadyExistsException {
        log.error("1");
        if (userRepository.existsByUsername(user.getUsername())) {
            log.error("Username {} already exists", user.getUsername());
            throw new UsernameAlreadyExistsException("Username already exists!");
        } else {
            log.error("2");
            String encodedPassword = bCryptPasswordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);
            userRepository.save(user);
        }
    }

    public void updateUser(User updatedUser) throws UsernameAlreadyExistsException, NoLoggedInUserException {
        User currentUser = getCurrentUser();
        log.info("Update user called for user: {}", currentUser);

        // Update username if provided and valid
        updateUsername(currentUser, updatedUser.getUsername());

        // Update favourite release year if provided
        String newFavouriteReleaseYear = updatedUser.getFavouriteReleaseYear();
        if (!StringUtils.isBlank(newFavouriteReleaseYear)) {
            currentUser.setFavouriteReleaseYear(newFavouriteReleaseYear);
            log.info("Updated user's favourite release year to '{}'.", newFavouriteReleaseYear);
        }

        // Save changes to database
        userRepository.save(currentUser);
        log.info("User updates saved successfully in the database.");
    }

    private void updateUsername(User currentUser, String newUsername) throws UsernameAlreadyExistsException {
        if (StringUtils.isBlank(newUsername)) {
            return; // No update needed if username is blank
        }

        String currentUsername = currentUser.getUsername();

        if (userRepository.existsByUsername(newUsername)) {
            log.warn("Attempt to change to a taken username: '{}'.", newUsername);
            throw new UsernameAlreadyExistsException("New username is taken by somebody else!");
        }

        if (newUsername.equals(currentUsername)) {
            log.warn("Attempt to change to the current username: '{}'.", currentUsername);
            throw new UsernameAlreadyExistsException("You cannot change to your existing username!");
        }

        currentUser.setUsername(newUsername);
        log.info("Changed username in the database from '{}' to '{}'.", currentUsername, newUsername);
    }

    public User getCurrentUser() throws NoLoggedInUserException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            log.error("No current user found");
            throw new NoLoggedInUserException("No current user found");
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    public void updateUserFavourites() throws NoLoggedInUserException, UsernameAlreadyExistsException {
        // find all movies
        List<MovieConnection> movies = movieService.getMovieConnectionsByUsernameInRatingOrder(getCurrentUser().getUsername());

        if (movies.isEmpty()) {
            return;
        }

        // count how many movies are saved to the profile by release year { "1990" : 2, "1991" : 4, "1992" : 8 }
        Map<String, Long> releaseYearCount = movies.stream()
                .map(movieConnection -> movieConnection.getMovie().getReleaseYear())
                .collect(Collectors.groupingBy(year -> year, Collectors.counting()));

        // return the most popular year
        String favouriteYear = releaseYearCount.entrySet().stream()
                .max(Map.Entry.comparingByValue()) // finds the map entry with highest value
                .map(Map.Entry::getKey) // gets teh key of that entry
                .orElse(null); // TODO: better handle null (throw error?)

        if (favouriteYear != null && !favouriteYear.isBlank()) {
            User updatedUser = new User();
            updatedUser.setFavouriteReleaseYear(favouriteYear);
            updateUser(updatedUser);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Return a UserDetails object without authorities
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
                new ArrayList<>());
    }

}
