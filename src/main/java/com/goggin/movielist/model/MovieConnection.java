package com.goggin.movielist.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "movieconnections")
public class MovieConnection {

    @Id
    @GeneratedValue
    @Column(name = "movie_connection_id")
    private Integer movie_connection_id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @Min(value = 0, message = "Score must be at least 0")
    @Max(value = 10, message = "Score must be at most 10")
    private double rating;

    public MovieConnection() {
    }

    public MovieConnection(User user, Movie movie,
            @Min(value = 0, message = "Score must be at least 0") @Max(value = 10, message = "Score must be at most 10") double rating) {
        this.user = user;
        this.movie = movie;
        this.rating = rating;
    }

    public Integer getMovie_connection_id() {
        return movie_connection_id;
    }

    public void setMovie_connection_id(Integer movie_connection_id) {
        this.movie_connection_id = movie_connection_id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "MovieConnection{" +
                "movieConnectionId=" + movie_connection_id +
                ", movie=" + (movie != null ? movie.getMovie_id() : "null") +
                ", user=" + (movie != null ? user.getUser_id() : "null") +
                ", rating=" + rating +
                '}';
    }

}
