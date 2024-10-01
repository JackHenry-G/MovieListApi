package com.goggin.movielist.respositories;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.goggin.movielist.model.Movie;
import com.goggin.movielist.model.MovieConnection;
import com.goggin.movielist.model.User;

@Repository
public interface MovieConnectionRepository extends JpaRepository<MovieConnection, Integer> {
    List<MovieConnection> findByUser_UsernameOrderByRatingDesc(String username);

    List<MovieConnection> findByUser_UsernameAndRatingGreaterThan(String username, double rating);

    Optional<MovieConnection> findByUserAndMovie(User user, Movie movie);

}
