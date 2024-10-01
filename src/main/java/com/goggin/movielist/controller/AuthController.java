package com.goggin.movielist.controller;

import com.goggin.movielist.config.JwtResponse;
import com.goggin.movielist.config.JwtUtils;
import com.goggin.movielist.exception.MovieAlreadySavedToUsersListException;
import com.goggin.movielist.exception.NoLoggedInUserException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.goggin.movielist.exception.UsernameAlreadyExistsException;
import com.goggin.movielist.model.Movie;
import com.goggin.movielist.model.User;
import com.goggin.movielist.service.MovieService;
import com.goggin.movielist.service.UserService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final MovieService movieService;

    public AuthController(final AuthenticationManager authenticationManager, final JwtUtils jwtUtils, final UserService userService, final MovieService movieService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
        this.movieService = movieService;
    }


    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody User loginRequest, BindingResult bindingResult) {
        log.info("Authenticating user: {}", loginRequest);

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("Validation error: " + bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        try {
            // authenticate user
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // generate and return JWT token
            String jwt = jwtUtils.generateJwtToken(authentication);
            User user = userService.getCurrentUser();

            log.info("User {} has successfully logged in and JWT token generated.", user.getUsername());
            return ResponseEntity
                    .ok(new JwtResponse(jwt, user.getUser_id(), user.getUsername(), user.getEmail()));
        } catch (BadCredentialsException e) {
            log.error("Bad credentials provided: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("This username and/or password combination cannot be found.");
        } catch (Exception e) {
            log.error("Issue signing in user: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Issue signing in user: " + e.getMessage());
        }
    }

    @GetMapping("/signuptest")
    public ResponseEntity<?> signUpTestUser() {
        try {
            User testUser = buildandSaveTestUser();

            log.info("Test user successfully signed up.");
            return ResponseEntity.ok(testUser);
        } catch (UsernameAlreadyExistsException e) {
            log.error("Test user already signed up!");
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (MovieAlreadySavedToUsersListException e) {
            log.error("This method already existed in the user's movie list: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("This movie already exists in your list so cannot be added again: " + e.getMessage());
        } catch (Exception e) {
            log.error("Issue signing up test user: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Issue with server: " + e.getMessage());
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody User user, BindingResult bindingResult) {
        log.info("Signing up user = {}", user);

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("Validation error: " + bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        try {
            // save user to database
            userService.saveNewUser(user);

            // save default movie to the database so user has one in place already
            Movie initialMovie = new Movie(230423, "La La Land", "2016-02-09", 128, "Ryan Gosling is the man",
                    "Romance", "/nlPCdZlHtRNcF6C9hzUH4ebmV1w.jpg", "/uDO8zWDhfWwoFdKS4fzkUJt0Rf0.jpg",
                    "Mia, an aspiring actress, serves lattes to movie stars in between auditions and Sebastian, a jazz musician, scrapes by playing cocktail party gigs in dingy bars, but as success mounts they are faced with decisions that begin to fray the fragile fabric of their love affair, and the dreams they worked so hard to maintain in each other threaten to rip them apart.");
            movieService.addMovieToUsersList(user, initialMovie, 8);

            log.info("User {} successfully signed up.", user.getUsername());
            return ResponseEntity.ok(user);
        } catch (UsernameAlreadyExistsException e) {
            log.error("User already signed up!");
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (MovieAlreadySavedToUsersListException e) {
                log.error("This method already existed in the user's movie list: {}", e.getMessage());
                return ResponseEntity.internalServerError().body("This movie already exists in your list so cannot be added again: " + e.getMessage());
        } catch (Exception e) {
            log.error("Issue signing up user: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Issue signing up user: " + e.getMessage());
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        log.info("Retrieving profile for user");

        try {
            User currentUser = userService.getCurrentUser();

            userService.updateUserFavourites();

            log.info("User {} profile successfully retrieved.", currentUser);
            return ResponseEntity.ok(currentUser); // TODO: don't want to return the password
        } catch (NoLoggedInUserException e) {
            log.error("Attempted to retrieve a profile without a logged in user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not logged in: " + e.getMessage());
        } catch (UsernameNotFoundException e) {
            log.error("Username of logged in user not found in database: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Username of logged in user not found in database" + e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving or updating user profile: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Issue getting user profile: " + e.getMessage());
        }
    }

    @PostMapping("/profile/edit")
    public ResponseEntity<?> editProfile(@ModelAttribute User user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("Validation error: " + bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        try {
            log.info("Attempting to update with {}", user);
            userService.updateUser(user);

            log.info("User edited successfully: {}", user);

            // Get the current authentication
            Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();

            // Generate a new JWT token using the current authentication
            String jwt = jwtUtils.generateJwtToken(currentAuth);

            // Return the new JWT token along with a success message and user details
            return ResponseEntity
                    .ok(new JwtResponse(jwt, user.getUser_id(), user.getUsername(), user.getEmail()));
        } catch (NoLoggedInUserException e) {
            log.error("Attempted to update a profile without a logged in user: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Attempted to update a profile without a logged in user: {}: " + e.getMessage());
        } catch (UsernameAlreadyExistsException e) {
            log.error("User edit UsernameAlreadyExistsException: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Username already exists.");
        } catch (Exception e) {
            log.error("Issue updating user due to unexpected error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Issue getting user profile: " + e.getMessage());
        }
    }

    private User buildandSaveTestUser() throws UsernameAlreadyExistsException, MovieAlreadySavedToUsersListException {
        User testUser = new User(1, "jackhenryg@hotmail.co.uk", "test", "pwd", 51.5074, -0.1278);
        userService.saveNewUser(testUser);
        log.info("Test user registered: {}", testUser);

        // save default movie to the database (need to change this all around)
        Movie laLaLand = new Movie(230423, "La La Land", "2016-02-09", 128, "Ryan Gosling is the man",
                "Romance", "/nlPCdZlHtRNcF6C9hzUH4ebmV1w.jpg", "/uDO8zWDhfWwoFdKS4fzkUJt0Rf0.jpg",
                "Mia, an aspiring actress, serves lattes to movie stars in between auditions and Sebastian, a jazz musician, scrapes by playing cocktail party gigs in dingy bars, but as success mounts they are faced with decisions that begin to fray the fragile fabric of their love affair, and the dreams they worked so hard to maintain in each other threaten to rip them apart.");
        Movie hungerGames = new Movie(230426, "Hunger Games", "2010-10-02", 128,
                "May the odds be ever in your favour", "Action", "/yDbyVT8tTOgXUrUXNkHEUqbxb1K.jpg",
                "/yXCbOiVDCxO71zI7cuwBRXdftq8.jpg",
                "Every year in the ruins of what was once North America, the nation of Panem forces each of its twelve districts to send a teenage boy and girl to compete in the Hunger Games.  Part twisted entertainment, part government intimidation tactic, the Hunger Games are a nationally televised event in which “Tributes” must fight with one another until one survivor remains.  Pitted against highly-trained Tributes who have prepared for these Games their entire lives, Katniss is forced to rely upon her sharp instincts as well as the mentorship of drunken former victor Haymitch Abernathy.  If she’s ever to return home to District 12, Katniss must make impossible choices in the arena that weigh survival against humanity and life against love. The world will be watching.");
        Movie goodfellas = new Movie(230493, "Goodfellas", "2000-10-01", 128, "Mobb bosses", "Crime",
                "/sw7mordbZxgITU877yTpZCud90M.jpg", "/aKuFiU82s5ISJpGZp7YkIr3kCUd.jpg",
                "The true story of Henry Hill, a half-Irish, half-Sicilian Brooklyn kid who is adopted by neighbourhood gangsters at an early age and climbs the ranks of a Mafia family under the guidance of Jimmy Conway.");
        Movie wonka = new Movie(230432, "Wonka", "2023-12-06", 117,
                "Every good thing in this world started with a dream.", "Crime",
                "/yOm993lsJyPmBodlYjgpPwBjXP9.jpg", "/qhb1qOilapbapxWQn9jtRCMwXJF.jpg",
                "Willy Wonka – chock-full of ideas and determined to change the world one delectable bite at a time – is proof that the best things in life begin with a dream, and if you’re lucky enough to meet Willy Wonka, anything is possible.");
        Movie wish = new Movie(230478, "Wish", "2023-12-03", 95, "Be careful what you wish for.",
                "Fantasy",
                "/ehumsuIBbgAe1hg343oszCLrAfI.jpg", "/AcoVfiv1rrWOmAdpnAMnM56ki19.jpg",
                "Asha, a sharp-witted idealist, makes a wish so powerful that it is answered by a cosmic force – a little ball of boundless energy called Star. Together, Asha and Star confront a most formidable foe - the ruler of Rosas, King Magnifico - to save her community and prove that when the will of one courageous human connects with the magic of the stars, wondrous things can happen.");

        movieService.addMovieToUsersList(testUser, laLaLand, 8);
        movieService.addMovieToUsersList(testUser, hungerGames, 8.9);
        // fav films
        movieService.addMovieToUsersList(testUser, goodfellas, 9.4); // not at cinema
        movieService.addMovieToUsersList(testUser, wonka, 9.1); // at cinema
        movieService.addMovieToUsersList(testUser, wish, 9.3); // at cinema
        log.info("Movies added to test user successfully");

        return testUser;
    }

}
