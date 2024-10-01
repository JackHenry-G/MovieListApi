package com.goggin.movielist.manual;

import com.goggin.movielist.exception.MovieAlreadySavedToUsersListException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.goggin.movielist.model.Movie;
import com.goggin.movielist.model.User;
import com.goggin.movielist.service.MovieService;
import com.goggin.movielist.service.UserService;
import com.goggin.movielist.service.UserTaskService;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserTaskServiceManualTest {

        @Autowired
        private UserTaskService userTaskService;

        @Autowired
        private MovieService movieService;

        @Autowired
        private UserService userService;

        // commented out and only used when I want to manually test the method
        @Test
        public void TestExecuteUserSpecificTasks() throws Exception, MovieAlreadySavedToUsersListException {
                // -- arrange ---
                User user = new User(1, "jackhenryg@hotmail.co.uk", "test", "pwd", 51.5074, -0.1278);
                userService.saveNewUser(user);

                // save default movie to the database (need to change this all around)
                Movie laLaLand = new Movie(230423, "La La Land", "2016-02-09", 128, "Ryan Gosling is the man",
                                "Romance", "/nlPCdZlHtRNcF6C9hzUH4ebmV1w.jpg", "/uDO8zWDhfWwoFdKS4fzkUJt0Rf0.jpg",
                                "Mia, an aspiring actress, serves lattes to movie stars in between auditions and Sebastian, a jazz musician, scrapes by playing cocktail party gigs in dingy bars, but as success mounts they are faced with decisions that begin to fray the fragile fabric of their love affair, and the dreams they worked so hard to maintain in each other threaten to rip them apart.");
                Movie hungerGames = new Movie(230426, "Hunger Games", "2010-10-02", 128,
                                "May the odds be ever in your favour", "Action", "/yDbyVT8tTOgXUrUXNkHEUqbxb1K.jpg",
                                "/yXCbOiVDCxO71zI7cuwBRXdftq8.jpg",
                                "Every year in the ruins of what was once North America, the nation of Panem forces each of its twelve districts to send a teenage boy and girl to compete in the Hunger Games.  Part twisted entertainment, part government intimidation tactic, the Hunger Games are a nationally televised event in which “Tributes” must fight with one another until one survivor remains.  Pitted against highly-trained Tributes who have prepared for these Games their entire lives, Katniss is forced to rely upon her sharp instincts as well as the mentorship of drunken former victor Haymitch Abernathy.  If she’s ever to return home to District 12, Katniss must make impossible choices in the arena that weigh survival against humanity and life against love. The world will be watching.");
                Movie goodfellas = new Movie(230493, "Goodfellas", "2000-10-01", 128, "Mobb bosses", "Crime",
                                "/sw7mordbZxgITU877yTpZCud90M.jpg", "/aKuFiU82s5ISJpGZp7YkIr3kCUd.jpg",
                                "The true story of Henry Hill, a half-Irish, half-Sicilian Brooklyn kid who is adopted by neighbourhood gangsters at an early age and climbs the ranks of a Mafia family under the guidance of Jimmy Conway.");
                Movie wonka = new Movie(230432, "Wonka", "2023-12-06", 117,
                                "Every good thing in this world started with a dream.", "Crime",
                                "/yOm993lsJyPmBodlYjgpPwBjXP9.jpg", "/qhb1qOilapbapxWQn9jtRCMwXJF.jpg",
                                "Willy Wonka – chock-full of ideas and determined to change the world one delectable bite at a time – is proof that the best things in life begin with a dream, and if you’re lucky enough to meet Willy Wonka, anything is possible.");
                Movie wish = new Movie(230478, "Wish", "2023-12-03", 95, "Be careful what you wish for.",
                                "Fantasy",
                                "/ehumsuIBbgAe1hg343oszCLrAfI.jpg", "/AcoVfiv1rrWOmAdpnAMnM56ki19.jpg",
                                "Asha, a sharp-witted idealist, makes a wish so powerful that it is answered by a cosmic force – a little ball of boundless energy called Star. Together, Asha and Star confront a most formidable foe - the ruler of Rosas, King Magnifico - to save her community and prove that when the will of one courageous human connects with the magic of the stars, wondrous things can happen.");
                Movie iceAge = new Movie(233409, "ICE AGE (2002)", "2023-12-03", 95, "Be careful what you wish for.",
                                "Fantasy",
                                "/ehumsuIBbgAe1hg343oszCLrAfI.jpg", "/AcoVfiv1rrWOmAdpnAMnM56ki19.jpg",
                                "Asha, a sharp-witted idealist, makes a wish so powerful that it is answered by a cosmic force – a little ball of boundless energy called Star. Together, Asha and Star confront a most formidable foe - the ruler of Rosas, King Magnifico - to save her community and prove that when the will of one courageous human connects with the magic of the stars, wondrous things can happen.");

                movieService.addMovieToUsersList(user, laLaLand, 8);
                movieService.addMovieToUsersList(user, hungerGames, 8.9);
                // fav films
                movieService.addMovieToUsersList(user, goodfellas, 9.4); // not at cinema
                movieService.addMovieToUsersList(user, wonka, 9.1); // at cinema
                movieService.addMovieToUsersList(user, wish, 9.3); // at cinema
                movieService.addMovieToUsersList(user, iceAge, 9.8); // at cinema

                // -- act ---
                userTaskService.scanVueCinemaAndSendEmail(user);
        }
}
