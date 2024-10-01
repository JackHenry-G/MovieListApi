package com.goggin.movielist.integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.goggin.movielist.model.Place;
import com.goggin.movielist.model.User;
import com.goggin.movielist.service.GooglePlacesApiService;

@SpringBootTest
public class GooglePlacesApiServiceIntegrationTest {

    @Autowired
    private GooglePlacesApiService googlePlacesApiService;

    @Test
    public void testGetPlaceFromGooglePlacesWithTextSearchForValidCinemaLocation() {
        User user = new User(1, "jackhenryg@hotmail.co.uk", "test", "pwd", 51.5074, -0.1278);
        List<Place> places = googlePlacesApiService.getPlaceFromGooglePlacesWithTextSearch("Vue Cinema", 8, user);

        boolean foundPicadillyVue = places.stream()
                .anyMatch(place -> place.getDisplayName().getText().contains("Vue Cinema London - Piccadilly"));

        assertNotNull(places);
        assertTrue(foundPicadillyVue);
    }

    @Test
    public void testGetPlaceFromGooglePlacesWithTextSearchWithNegativeResultsAmount() {
        User user = new User(1, "jackhenryg@hotmail.co.uk", "test", "pwd", 51.5074, -0.1278);

        assertThrows(IllegalArgumentException.class,
                () -> googlePlacesApiService.getPlaceFromGooglePlacesWithTextSearch("Vue Cinema", -1, user));
    }

    @Test
    public void testGetPlaceFromGooglePlacesWithTextSearchWithInvalidTextSet() {
        User user = new User(1, "jackhenryg@hotmail.co.uk", "test", "pwd", 51.5074, -0.1278);

        List<Place> result = googlePlacesApiService.getPlaceFromGooglePlacesWithTextSearch("12a-w-923n", 2, user);
        assertTrue(result.isEmpty());
    }
}
