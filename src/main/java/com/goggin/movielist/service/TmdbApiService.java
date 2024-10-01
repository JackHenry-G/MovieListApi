package com.goggin.movielist.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.goggin.movielist.exception.MovieNotFoundInTmdbException;
import com.goggin.movielist.model.Movie;
import com.goggin.movielist.model.TmdbResponse;
import com.goggin.movielist.model.TmdbResponseResult;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TmdbApiService {

    @Value("${tmdb.api.key}")
    private String apiKey;

    @Value("${tmdb.api.base.url}")
    private String baseUrl;

    @Autowired
    private RestTemplate restTemplate;

    public List<TmdbResponseResult> getMoviesFromTmdbByTitle(String title) {
        Map<String, String> requestParams = Collections.singletonMap("query", title);
        return getMoviesFromTmdb("/search/movie", null, requestParams);
    }

    public List<TmdbResponseResult> getMoviesFromTmdbByYear(String year) {
        Map<String, String> requestParams = Collections.singletonMap("primary_release_year", year);
        return getMoviesFromTmdb("/discover/movie", null, requestParams);
    }

    public Movie getMovieDetailsFromTmdbById(Integer tmdbMovieId) throws MovieNotFoundInTmdbException {
        String url = buildUrl("/movie", Integer.toString(tmdbMovieId), null);
        HttpEntity<String> entity = createHttpEntity();

        try {
            ResponseEntity<Movie> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, Movie.class);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                log.info("Movie retrieved successfully from tmdb: {}", responseEntity.getBody());
                return responseEntity.getBody();
            } else {
                log.warn("Failed to retrieve movie, status code: {}", responseEntity.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.info("Movie not found for ID: {} due to {}", tmdbMovieId, e.getMessage());
                throw new MovieNotFoundInTmdbException("Movie with ID " + tmdbMovieId + " not found.");
            } else {
                log.error("HttpClientErrorExcpetion when getting movie from tMDB: {}", e.getMessage());
            }
        } catch (RestClientException e) {
            log.error("Error getting movie from tMDB: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error getting movie from tMDB for ID {}. Details: {}", tmdbMovieId, e.getMessage());
        }

        return null;
    }

    private List<TmdbResponseResult> getMoviesFromTmdb(String endpoint, String pathSegment,
            Map<String, String> requestParams) {
        String url = buildUrl(endpoint, pathSegment, requestParams);
        HttpEntity<String> entity = createHttpEntity();

        try {
            ResponseEntity<TmdbResponse> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity,
                    TmdbResponse.class);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                log.info("Movies retrieved successfully from tmdb: {}", responseEntity.getBody().getResults());
                return responseEntity.getBody().getResults();
            } else {
                log.warn("Failed to retrieve movie, status code: {}", responseEntity.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Unexpected error getting movies from tMDB search. Details: {}", e.getMessage());
        }

        return null;
    }

    private String buildUrl(String endpoint, String pathSegment, Map<String, String> requestParams) {
        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromHttpUrl(baseUrl + endpoint)
                .queryParam("include_adult", "false")
                .queryParam("include_video", "false")
                .queryParam("language", "en-US");

        if (pathSegment != null) {
            urlBuilder.pathSegment(pathSegment);
        } else if (requestParams != null && !requestParams.isEmpty()) {
            requestParams.forEach(urlBuilder::queryParam); // add the dynamic request params
        }

        return urlBuilder.toUriString();
    }

    private HttpEntity<String> createHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", apiKey);
        return new HttpEntity<>(headers);
    }

}