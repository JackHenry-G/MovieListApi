package com.goggin.movielist.unit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.goggin.movielist.model.Place;
import com.goggin.movielist.model.PlaceResponse;
import com.goggin.movielist.model.User;
import com.goggin.movielist.model.Place.DisplayName;
import com.goggin.movielist.service.GooglePlacesApiService;

@ExtendWith(MockitoExtension.class)
public class GooglePlacesApiServiceUnitTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GooglePlacesApiService googlePlacesApiService;

    @Test
    public void testGetPlaceFromGooglePlacesWithTextSearchForValidCinemaLocation() {
        String url = "https://places.googleapis.com/v1/places:searchText";

        User user = new User(1, "jackhenryg@hotmail.co.uk", "test", "pwd", 51.5074, -0.1278);

        Place place1 = new Place("https://www.myvue.com/cinema/piccadilly/whats-on",
                new DisplayName("Vue Cinema London - Piccadilly", "en-US"));

        Place place2 = new Place("https://www.myvue.com/cinema/leicester-square/whats-on",
                new DisplayName("Vue Cinema London - Leicester Square", "en-US"));
        List<Place> places = Arrays.asList(place1, place2);
        PlaceResponse mockPlaceResponse = new PlaceResponse();
        mockPlaceResponse.setPlaces(places);

        ResponseEntity<PlaceResponse> mockResponseEntity = new ResponseEntity<>(mockPlaceResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(eq(url), any(HttpEntity.class), eq(PlaceResponse.class)))
                .thenReturn(mockResponseEntity);

        List<Place> results = googlePlacesApiService.getPlaceFromGooglePlacesWithTextSearch("Vue Cinema", 8, user);

        boolean foundPicadillyVue = results.stream()
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
        String url = "https://places.googleapis.com/v1/places:searchText";

        User user = new User(1, "jackhenryg@hotmail.co.uk", "test", "pwd", 51.5074, -0.1278);

        PlaceResponse mockPlaceResponse = new PlaceResponse();
        mockPlaceResponse.setPlaces(null);

        ResponseEntity<PlaceResponse> mockResponseEntity = new ResponseEntity<>(mockPlaceResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(eq(url), any(HttpEntity.class), eq(PlaceResponse.class)))
                .thenReturn(mockResponseEntity);

        List<Place> result = googlePlacesApiService.getPlaceFromGooglePlacesWithTextSearch("12a-w-923n", 2, user);
        assertTrue(result.isEmpty());
    }
}
