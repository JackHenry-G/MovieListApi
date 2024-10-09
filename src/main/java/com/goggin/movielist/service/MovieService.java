package com.goggin.movielist.service;

import java.util.*;
import java.util.stream.Collectors;

import com.goggin.movielist.exception.GenresNotFoundException;
import com.goggin.movielist.exception.MovieAlreadySavedToUsersListException;
import com.goggin.movielist.model.Genre;
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
    private final GenreService genreService;

    public MovieService(MovieRepository movieRepository, MovieConnectionRepository movieConnectionRepository,
                        MovieConnectionService movieConnectionService, GenreService genreService) {
        this.movieRepository = movieRepository;
        this.movieConnectionRepository = movieConnectionRepository;
        this.movieConnectionService = movieConnectionService;
        this.genreService = genreService;
    }

    // CREATE operations ---------------
    public Movie addMovieToUsersList(User user, Movie movie, double rating) throws MovieAlreadySavedToUsersListException, GenresNotFoundException {
        log.info("Attempting to add '{}' with rating of {} to {}'s list.", movie.getTitle(), rating, user.getUsername());

        // Retrieve the movie from the database, or add it if it doesn't exist
        Movie dbMovie = this.movieRepository.findByTitle(movie.getTitle());
        if (dbMovie == null) {

            Set<Genre> movie_genres = movie.getGenres();
            if (movie_genres != null && !movie_genres.isEmpty()) {
                Set<Genre> dbGenres = new HashSet<>();
                for (Genre genre : movie_genres) {
                    Genre dbGenre = genreService.findOrCreateGenre(genre);
                    dbGenres.add(dbGenre);
                }
                movie.setGenres(dbGenres);
            } else {
                throw new GenresNotFoundException("No genres were found for this movie");
            }

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
