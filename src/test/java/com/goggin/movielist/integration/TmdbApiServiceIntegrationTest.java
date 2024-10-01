package com.goggin.movielist.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.goggin.movielist.exception.MovieNotFoundInTmdbException;
import com.goggin.movielist.model.Movie;
import com.goggin.movielist.model.TmdbResponseResult;
import com.goggin.movielist.service.TmdbApiService;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
public class TmdbApiServiceIntegrationTest {

    @Autowired
    private TmdbApiService tmdbApiService;

    @Test
    public void testGetMovieDetailsFromTmdbById_validId() throws MovieNotFoundInTmdbException {
        // arrange
        Integer jurassicParkId = 329;
        log.info("Testing TMDB API by getting movie with valid ID for Jurassic Park");

        // act
        Movie movie = tmdbApiService.getMovieDetailsFromTmdbById(jurassicParkId);
        log.info("Received movie details = {}", movie);

        // assert
        assertNotNull(movie);
        assertEquals(jurassicParkId, movie.getTmdb_id());
        assertEquals("Jurassic Park", movie.getTitle());
    }

    @Test
    public void testGetMovieDetailsFromTmdbById_invalidId() throws MovieNotFoundInTmdbException {
        // arrange
        Integer invalidMovieId = -1;

        // act
        assertThrows(MovieNotFoundInTmdbException.class, () -> {
            tmdbApiService.getMovieDetailsFromTmdbById(invalidMovieId);
        });

    }

    @Test
    public void testGetMoviesFromTmdbSearchByName() throws MovieNotFoundInTmdbException {
        // arrange
        String searchTitle = "Jurassic Park";
        log.info("Testing TMDB API by getting search for title Jurassic Park");

        // act
        Iterable<TmdbResponseResult> foundMovies = tmdbApiService.getMoviesFromTmdbByTitle(searchTitle);

        // assert
        assertNotNull(foundMovies);
        for (TmdbResponseResult movie : foundMovies) {
            log.info("Received movie details = {}", movie.getTitle());
            assertNotNull(movie.getId(), "Movie ID should not be null");
            assertNotNull(movie.getTitle(), "Movie Title should not be null");

        }

    }

    @Test
    public void testGetMoviesFromTmdbByYear() throws MovieNotFoundInTmdbException {
        // arrange
        String year = "1999";

        // act
        Iterable<TmdbResponseResult> foundMovies = tmdbApiService.getMoviesFromTmdbByYear(year);

        // assert
        assertNotNull(foundMovies);
        for (TmdbResponseResult movie : foundMovies) {
            log.info("Received movie details = {}", movie.getRelease_date());
            assertEquals(year, movie.getRelease_date().substring(0, 4));
        }

    }

}
