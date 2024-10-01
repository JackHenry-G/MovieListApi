package com.goggin.movielist.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.goggin.movielist.model.Movie;
import com.goggin.movielist.model.MovieConnection;
import com.goggin.movielist.model.User;
import com.goggin.movielist.respositories.MovieConnectionRepository;

@Service
public class MovieConnectionService {

    private final MovieConnectionRepository movieConnectionRepository;

    public MovieConnectionService(MovieConnectionRepository movieConnectionRepository) {
        this.movieConnectionRepository = movieConnectionRepository;
    }

    public MovieConnection saveMovieConnection(MovieConnection movieConnectionRepository) {
        return this.movieConnectionRepository.save(movieConnectionRepository);
    }

    public Optional<MovieConnection> findMovieConnectionBetweenMovieAndConnection(User user, Movie movie) {
        return movieConnectionRepository.findByUserAndMovie(user, movie);
    }

    public Optional<MovieConnection> findMovieConnectionById(Integer id) {
        return movieConnectionRepository.findById(id);
    }

    public void deleteMovieConnectionById(Integer connectionId) throws Exception {
        try {
            movieConnectionRepository.deleteById(connectionId);
        } catch (Exception e) {
            // Handle exceptions if the deletion fails, e.g., connectionId not found
            throw new Exception("MovieConnection with ID " + connectionId + " not found.");
        }
    }
}
