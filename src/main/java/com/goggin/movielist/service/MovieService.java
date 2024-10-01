package com.goggin.movielist.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.goggin.movielist.exception.MovieAlreadySavedToUsersListException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.goggin.movielist.model.Movie;
import com.goggin.movielist.model.MovieConnection;
import com.goggin.movielist.model.User;
import com.goggin.movielist.respositories.MovieConnectionRepository;
import com.goggin.movielist.respositories.MovieRepository;

@Service
@Slf4j
public class MovieService {
    private final MovieRepository movieRepository;
    private final MovieConnectionRepository movieConnectionRepository;
    private final MovieConnectionService movieConnectionService;

    public MovieService(MovieRepository movieRepository, MovieConnectionRepository movieConnectionRepository,
            MovieConnectionService movieConnectionService) {
        this.movieRepository = movieRepository;
        this.movieConnectionRepository = movieConnectionRepository;
        this.movieConnectionService = movieConnectionService;
    }

    // CREATE operations ---------------
    public Movie addMovieToUsersList(User user, Movie movie, double rating) throws MovieAlreadySavedToUsersListException {
        log.info("Attempting to add '{}' with rating of {} to {}'s list.", movie.getTitle(), rating, user.getUsername());

        // Retrieve the movie from the database, or add it if it doesn't exist
        Movie dbMovie = this.movieRepository.findByTitle(movie.getTitle());
        if (dbMovie == null) {
            dbMovie = this.movieRepository.save(movie);
            log.info("Movie '{}' was not found in the database. Adding it.", movie.getTitle());
        } else {
            log.info("Movie '{}' found in the database.", dbMovie.getTitle());
        }

        // Check if the movie is already in the user's list
        if (findSavedMovieByUser(user, dbMovie).isPresent()) {
            log.warn("Movie '{}' is already saved to {}'s list. Throwing exception.", dbMovie.getTitle(), user.getUsername());
            throw new MovieAlreadySavedToUsersListException("Movie is already saved to the user's list.");
        }

        // Create connection, assign a rating, and associate with the user
        log.info("Creating connection for movie '{}' with rating {}.", dbMovie.getTitle(), rating);
        MovieConnection movieConnection = new MovieConnection(user, dbMovie, rating);
        this.movieConnectionService.saveMovieConnection(movieConnection);

        log.info("Movie connection saved. Returning movie '{}'.", dbMovie.getTitle());
        return movieConnection.getMovie();
    }


    public Iterable<Movie> addMultipleMoviesToList(Iterable<Movie> movies) {
        return this.movieRepository.saveAll(movies);
    }

    public Optional<Movie> findSavedMovieByUser(User user, Movie movie) {
        // TODO: convert this method to a SQL statement
        List<MovieConnection> moviesConnections = getMovieConnectionsByUsernameInRatingOrder(user.getUsername());

        // Search for the movie in the connections
        return moviesConnections.stream()
                .map(MovieConnection::getMovie) // Get the Movie from MovieConnection
                .filter(savedMovie -> Objects.equals(savedMovie.getMovie_id(), movie.getMovie_id())) // Filter by movie ID
                .findFirst(); // Return the first matching movie as an Optional
    }

    public List<MovieConnection> getMovieConnectionsByUsernameInRatingOrder(String username) {
        return movieConnectionRepository
                .findByUser_UsernameOrderByRatingDesc(username);
    }

    public List<Movie> getMoviesWhereRatingIsGreaterThan(String username, double rating) {
        // TODO: convert this method to a SQL statement
        List<MovieConnection> movieConnections = movieConnectionRepository
                .findByUser_UsernameAndRatingGreaterThan(username, rating);

        return movieConnections.stream()
                .map(MovieConnection::getMovie)
                .filter(Objects::nonNull) // Filter out null movies, if any
                .collect(Collectors.toList());
    }

    public Movie getMovieById(Integer id) {
        return this.movieRepository.findById(id).orElseThrow();
    }
}
