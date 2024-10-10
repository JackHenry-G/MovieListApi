package com.goggin.movielist.controller;

import com.goggin.movielist.model.TmdbResponseResult;
import com.goggin.movielist.service.TmdbApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tmdb/search")
@Slf4j
public class TmdbApiController {

    private final TmdbApiService tmdbApiService;

    public TmdbApiController(TmdbApiService tmdbApiService) {
        this.tmdbApiService = tmdbApiService;
    }

    @GetMapping("/moviesByTitle")
    public ResponseEntity<?> searchTmdbForMoviesByTitle(@RequestParam String movieTitle) {
        log.info("Query request param (movie title) = {}", movieTitle);

        try {
            List<TmdbResponseResult> movies = tmdbApiService.getMoviesFromTmdbByTitle(movieTitle);
            if (movies != null) {
                return new ResponseEntity<>(movies, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT); // or HttpStatus.NOT_FOUND
            }
        } catch (Exception e) {
            log.error("Issue with search for a movie from TMDB by the movie title: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Issue with search for a movie from TMDB by the movie title: " + e.getMessage());
        }
    }

    @GetMapping("/moviesByParams")
    public ResponseEntity<?> searchForTmdbMoviesByParams(@RequestParam Map<String, String> params) {
        log.info("Query request params = {}", params);

        try {
            // Pass the dynamic parameters to the service
            List<TmdbResponseResult> movies = tmdbApiService.getMoviesFromTmdbByParam(params);

            if (movies != null && !movies.isEmpty()) {
                return new ResponseEntity<>(movies, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT); // or HttpStatus.NOT_FOUND
            }
        } catch (Exception e) {
            log.error("Issue with discover for movies from TMDB: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Issue with search for movies from TMDB: " + e.getMessage());
        }
    }

    @GetMapping("/popular-movies")
    public ResponseEntity<?> getPopularTmdbMovies() {
        log.info("Getting popular movies");

        try {
            // Pass the dynamic parameters to the service
            List<TmdbResponseResult> movies = tmdbApiService.getPopularMovies();

            if (movies != null && !movies.isEmpty()) {
                return new ResponseEntity<>(movies, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT); // or HttpStatus.NOT_FOUND
            }
        } catch (Exception e) {
            log.error("Issue with search for popular movies from TMDB: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Issue with search for movies from TMDB: " + e.getMessage());
        }
    }

}
