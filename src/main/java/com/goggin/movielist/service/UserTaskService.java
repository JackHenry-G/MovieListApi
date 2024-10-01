package com.goggin.movielist.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.goggin.movielist.model.Place;
import com.goggin.movielist.model.User;
import com.goggin.movielist.respositories.UserRepository;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserTaskService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private MovieService movieService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GooglePlacesApiService googlePlacesApiService;

    // @Scheduled(cron = "0 0 0 * * ?")
    public void executeUserSpecificTasks() {
        List<User> users = userRepository.findAll(); // Fetch all users

        // run jobs for each user
        for (User user : users) {
            scanVueCinemaAndSendEmail(user);
        }
    }

    public void scanVueCinemaAndSendEmail(User user) {
        log.info("scanVueCinemaAndSendEmail cron job started...");

        // setup user information
        String recipientUsername = user.getUsername();
        String recipientEmail = user.getEmail();
        List<Place> localVueCinemas = googlePlacesApiService.getPlaceFromGooglePlacesWithTextSearch("Vue Cinema",
                8, user); // get user's local vue cinemas
        List<String> favFilmTitles = movieService
                .getMoviesWhereRatingIsGreaterThan(recipientUsername, 9)
                .stream()
                .map(movie -> movie.getTitle().toUpperCase())
                .collect(Collectors.toList()); // get user's favourite films in their movielist
        log.info("User's information successfully collected!");

        // initilize message holders
        Map<String, List<String>> movieAndCinemas = new HashMap<>(); // {"Wonka" : ["Vue Thurrock", "Vue Westfield"]}

        // initialize selenium web driver
        log.info("initializing selenium web driver...");
        WebDriverManager.chromedriver().clearDriverCache().setup();
        WebDriver driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5)); // Set it once here
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10)); // Explicit wait
        log.info("Selenium web driver succesfully initialized, beginning searches on the below cinemas:");
        localVueCinemas.forEach(cinema -> log.info(cinema.getDisplayName().getText()));

        try {
            // search each lobal cinema website for the favourite movies
            for (Place cinema : localVueCinemas) {

                // get website information
                String cinemaWebsite = cinema.getWebsiteUri();
                String cinemaName = cinema.getDisplayName().getText();
                log.info("Starting process with - {}", cinemaName);

                // go to website
                driver.get(cinemaWebsite);
                log.info("Website driver retrieved");

                // reject all cookies
                WebElement rejectAllCookiesButton = driver.findElement(By.id("onetrust-reject-all-handler"));
                rejectAllCookiesButton.click();
                log.info("Reject cookies button found and clicked.");

                // click button to go on the full 'all times' section
                WebElement allTimesButton = driver
                        .findElement(By.cssSelector("button[data-test='filters-day-All Times']"));
                allTimesButton.click();

                // find matching movies on website
                wait.until(ExpectedConditions
                        .visibilityOfAllElementsLocatedBy(By.cssSelector(".showing-item.showing-listing-item")));
                List<WebElement> showings = driver.findElements(By.cssSelector(".showing-item.showing-listing-item"));

                for (WebElement showing : showings) {
                    String showingTitle = showing.findElement(By.className("film-heading__title")).getText();

                    // if a film we were looking for
                    if (favFilmTitles.contains(showingTitle)) {
                        // Split the URL by slashes '/'
                        String[] parts = cinemaWebsite.split("/");
                        String location = parts[parts.length - 2];

                        String showingTimesLink = "https://www.myvue.com/cinema/" + location + "/film/"
                                + showingTitle.toLowerCase();

                        movieAndCinemas.computeIfAbsent(showingTitle, k -> new ArrayList<>())
                                .add("- " + cinemaName + ": " + showingTimesLink);
                    }
                }
                driver.manage().deleteAllCookies();
            }
            driver.quit();
            log.info("Selenium searches successful!");
        } catch (NoSuchElementException e) {
            log.error("CinemaScan job NoSuchElementException: {}", e.getMessage());
        } catch (ElementNotInteractableException e) {
            log.error("CinemaScan job ElementNotInteractableException: {}", e.getMessage());
        } catch (TimeoutException e) {
            log.error("CinemaScan job TimeoutException: {}", e.getMessage());
        } catch (WebDriverException e) {
            log.error("CinemaScan job WebDriverException: {}", e.getMessage());
        } catch (Exception e) {
            log.error("CinemaScan job Exception: {}", e.getMessage());
        }

        // build message for each movie e.g. "Goodfellas is showing at Cinema1, Cinema2"
        StringBuilder messageBuilder = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : movieAndCinemas.entrySet()) {
            String movieTitle = entry.getKey();
            List<String> showingLocations = entry.getValue();

            messageBuilder.append(movieTitle).append(" is showing at:\n");
            for (String showingLocation : showingLocations) {
                messageBuilder.append(showingLocation).append("\n");
            }
            messageBuilder.append("\n\n");
        }

        log.info("Beginning email process!");
        // send email if found matching movies
        if (messageBuilder.isEmpty()) {
            log.info("No movies found at the cinemas! Email therefore NOT sent.");
        } else {
            emailService.sendEmail(recipientEmail, "Upcoming movies!", "Hi " + recipientUsername
                    + ", \n\nThis is your automated CinemaScanner service here to tell you which of your favourite movies, according to your FavFilms list, are showing in a cinema near you! Check them out below :) \n\n"
                    + messageBuilder + "Thanks,\nMovieListCinemaScanner");
        }

        log.info("scanVueCinemaAndSendEmail cron job finished");
    }

}
