# The MovieList Rest API

This REST API is designed for managing a list of every movie you have ever watched, the rating out of 10 you gave it and additional thoughts about the movie. 

It is also designed to allow you to search for any movie from 'The Movie Database (tMDB)' service and add any movie to that list.

A cronjob script has also been created to web scrape cinema showtimes and return any flagged movies, from the user's list, showing in a cinema located near to the user. This means you'll never miss a movie you want to see.

This can be interacted with via any RESTful means, but a front-end has been implented with REACT @ https://github.com/JackHenry-G/my-movielist-in-react


A demo application of this has been deployed @ (IN PROGRESS)

## Features

- **User accounts:** Users can create and log into their movielist account.
- **Search Movies:** Users can search for a movie they just watched through the tMDB API.
- **Rate Movies:** Users can then add that movie to their favourite films list by giving it a score out of 10.
- **Ranking System:** Users can see all the movies in their list, ranked in order of highest rating to lowest.
- **Cinema Notifications:** Everyday at midnight a cronjob script will run, which will tell the user if one of their favourite films (>9 rating) is showing in a cinema near them. 

## Technologies

- **Backend:**

  - Spring Boot
  - Java
  - Docker

- **Frontend:**

  - HTML
  - CSS
  - JavaScript
  - REACT

- **Database:**

  - PostgreSQL

- **3rd party APIs:**

  - Google Places API - to search for cinemas near to the user
  - tMDB - movie database to provide data for the app

## Getting started on your local machine

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/JackHenry-G/MovieListApi.git
   cd movielist
   ```
2. **Setup postgreSQL**

   Before running the application, you need to setup a data source. You can do this using a PostgreSQL database, which I have provided within the docker-compose file.

   1. Ensure you have docker installed on your machine
   2. Set the correct profile in the parent 'application.properties' file:
      - Use 'test' if you want to have capabilities for quicker testing, like an automated register of a test user.
      - Use 'staging' if you want the production app but with a test database, but without test feature capabilities.
        Both of these include a docker postgresql intance set to 'create-drop' so the database will be cleared of data and setup a new when the app is restarted.
   3. docker-compose up -d
   4. ./mvnw spring-boot:run

   ![Docker setup][movielist_docker_setup.png]

3. **Access the app**
   You can now hit this rest api at http://localhost:8080. If you are having issues check the allowed origins in 'WebConfig'.
