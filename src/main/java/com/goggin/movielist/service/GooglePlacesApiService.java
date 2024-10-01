package com.goggin.movielist.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.goggin.movielist.model.Place;
import com.goggin.movielist.model.PlaceResponse;
import com.goggin.movielist.model.User;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GooglePlacesApiService {

    private final RestTemplate restTemplate;

    public GooglePlacesApiService(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Place> getPlaceFromGooglePlacesWithTextSearch(String textQuery, Integer maxResultCount, User user) {
        if (maxResultCount <= 0) {
            throw new IllegalArgumentException("There can only be an amount of results great than zero");
        }

        // configure API details
        String url = "https://places.googleapis.com/v1/places:searchText";
        String apiKey = "AIzaSyAuEBLhs17AGsxqD2ttmekY7Q0Fa3Vb6Ns";

        // setup headers
        HttpHeaders headers = new HttpHeaders();
        headers.add("accept", "application/json");
        headers.add("X-Goog-Api-Key", apiKey);
        headers.add("X-Goog-FieldMask", "places.displayName,places.websiteUri");

        // request body parameters
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("textQuery", textQuery);
        requestBody.put("maxResultCount", maxResultCount);

        Map<String, Object> locationBias = new HashMap<>();
        Map<String, Object> circle = new HashMap<>();
        Map<String, Object> center = new HashMap<>();
        center.put("latitude", user.getLatitude());
        center.put("longitude", user.getLongitude());
        circle.put("center", center);
        locationBias.put("circle", circle);
        requestBody.put("locationBias", locationBias);

        HttpEntity<Map<String, Object>> requestData = new HttpEntity<>(requestBody, headers);

        ResponseEntity<PlaceResponse> responseEntity = restTemplate.postForEntity(url, requestData,
                PlaceResponse.class);

        if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
            List<Place> places = responseEntity.getBody().getPlaces();

            if (places != null && !places.isEmpty()) {
                log.info("Successful Google Places API request. Status code: {}", responseEntity.getStatusCode());
                return places;
            } else {
                log.warn("Google Places API request successful but no places data found. Status code: {}",
                        responseEntity.getStatusCode());
                return Collections.emptyList(); // Return an empty list instead of null
            }
        } else {
            log.error("Error while making the Google Places API request. Status code: {}",
                    responseEntity.getStatusCode());
            return null;
        }

    }

}
