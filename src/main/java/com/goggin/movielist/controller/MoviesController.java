package com.goggin.movielist.controller;

import com.goggin.movielist.exception.MovieAlreadySavedToUsersListException;
import com.goggin.movielist.exception.NoLoggedInUserException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.goggin.movielist.model.Movie;
import com.goggin.movielist.model.MovieConnection;
import com.goggin.movielist.service.MovieConnectionService;
import com.goggin.movielist.service.MovieService;
import com.goggin.movielist.service.TmdbApiService;
import com.goggin.movielist.service.UserService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.validation.BindingResult;

@RestController
@RequestMapping("/api/v1/user/movies")
@Slf4j
public class MoviesController {

    private final UserService userService;
    private final MovieService movieService;
    private final TmdbApiService tmdbApiService;
    private final MovieConnectionService movieConnectionService;

    public MoviesController(UserService userService, MovieService movieService, TmdbApiService tmdbApiService, MovieConnectionService movieConnectionService) {
        this.userService = userService;
        this.movieService = movieService;
        this.tmdbApiService = tmdbApiService;
        this.movieConnectionService = movieConnectionService;
    }

    @GetMapping("/")
    public ResponseEntity<?> getUsersMovies(Principal principal) {
        try {
            List<MovieConnection> movieConnections = movieService
                    .getMovieConnectionsByUsernameInRatingOrder(principal.getName());

            if (!movieConnections.isEmpty()) {
                return ResponseEntity.ok(movieConnections);
            } else {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT); // or HttpStatus.NOT_FOUND
            }
        } catch (Exception e) {
            log.error("Issue with getting your movies: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Issue with getting your movies: " + e.getMessage());
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> addMovieToList(@RequestParam Integer tmdbMovieId, @RequestParam double rating) {
        try {
            Movie tmdbMovie = tmdbApiService.getMovieDetailsFromTmdbById(tmdbMovieId);
            log.info("Movie added to user: {}", tmdbMovie.toString());

            Movie usersListMovie = movieService.addMovieToUsersList(userService.getCurrentUser(), tmdbMovie,
                    rating);

            // must update profile for movie suggestion functions
            userService.updateUserFavourites();

            return ResponseEntity.ok(usersListMovie.getTitle() + " added to your list!");
        } catch (NoLoggedInUserException e) {
            log.error("Was unable to update the profile after saving the movie to the users list: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("There was an issue updating your profile. Please contact your admin!: ");
        } catch (MovieAlreadySavedToUsersListException e) {
            log.error("This movie already existed in the user's movie list and should not be added again: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("This movie already exists in your list!: ");
        } catch (Exception e) {
            log.error("Issue with adding a movie to your list: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Issue with adding a movie to your list: " + e.getMessage());
        }
    }

    @PostMapping("/{movieConnectionId}/edit-rating")
    public ResponseEntity<?> editMovieRating(
            @PathVariable Integer movieConnectionId,
            @RequestParam double rating) {
        try {
            MovieConnection movieConnection = movieConnectionService.findMovieConnectionById(movieConnectionId)
                    .orElseThrow(() -> new RuntimeException("MovieConnection not found"));

            movieConnection.setRating(rating);
            movieConnectionService.saveMovieConnection(movieConnection);

            return ResponseEntity.ok("Rating updated successfully!");
        } catch (Exception e) {
            log.error("Issue with updating movie rating: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Issue with updating movie rating" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}/remove")
    public ResponseEntity<?> removeMovieFromUsersList(@PathVariable Integer id) {
        try {
            Movie movieToDeleteFromUsersList = movieService.getMovieById(id);

            MovieConnection movieConnection = movieConnectionService
                    .findMovieConnectionBetweenMovieAndConnection(userService.getCurrentUser(),
                            movieService.getMovieById(movieToDeleteFromUsersList.getMovie_id()))
                    .orElseThrow(() -> new RuntimeException("MovieConnection not found"));

            movieConnectionService.deleteMovieConnectionById(movieConnection.getMovie_connection_id());

            return ResponseEntity.ok(movieToDeleteFromUsersList.getTitle() + " - has been removed from your list!");
        } catch (Exception e) {
            log.error("Issue with removing the movie from your list: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Issue with removing the movie from your list: " + e.getMessage());
        }
    }

    // ------------------------------- TEST ---------------------------------------

    @GetMapping("/{movieId}") // return specific movie to view
    public ResponseEntity<?> getMovie(@PathVariable Integer movieId) {
        return ResponseEntity.ok(movieService.getMovieById(movieId));
    }

    @PostMapping("/multimovies") // add multiple movies in one request
    public ResponseEntity<?> addMultiMovies(@RequestBody Iterable<Movie> movies, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            // Handle validation errors and return an appropriate response
            // for example, if a score of > 10 is entered.
            return ResponseEntity.badRequest()
                    .body("Validation error: " + bindingResult.getAllErrors().get(0).getDefaultMessage());
        } else {
            Iterable<Movie> savesMovies = movieService.addMultipleMoviesToList(movies);
            return ResponseEntity.ok(savesMovies);
        }
    }
}
