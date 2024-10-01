package com.goggin.movielist.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "movies")
@ToString // use Lombok to generate a ToString method without having to write it ourselves
@Data
@NoArgsConstructor
public class Movie {

    @Id // used to mark it as the primary key of the table
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "movie_id")
    private Integer movie_id; // only integer as takes less storage and am not anticipating loads of accounts

    @JsonProperty("id")
    @Column(name = "tmdb_id")
    private Integer tmdb_id;

    @Column(unique = true)
    private String title;

    @JsonProperty("release_date") // api passes back as release date
    private String releaseYear;

    private Integer runtime; // e.g. 157, (Minutes)
    private String tagline; // e.g. "Everyone hungers for something."

    private String genre; // returned by TMDB

    private String backdrop_path; // image backdrop URL
    private String poster_path; // image post URL

    @Column(columnDefinition = "TEXT") // this string is a long piece of text. In SQL 'Text' is used to store bigger
                                       // bits of text data (over 255 characters)
    private String overview; // rundown of movie

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<MovieConnection> userRatings;

    public Movie(Integer movie_id, String title, String releaseYear, Integer runtime, String tagline, String genre,
            String backdrop_path, String poster_path, String overview) {
        this.movie_id = movie_id;
        this.title = title;
        this.releaseYear = releaseYear.substring(0, 4);
        this.runtime = runtime;
        this.tagline = tagline;
        this.genre = genre;
        this.backdrop_path = "https://image.tmdb.org/t/p/w500" + backdrop_path;
        this.poster_path = "https://image.tmdb.org/t/p/w500" + poster_path;
        this.overview = overview;
    }

    public void setReleaseYear(String releaseYear) {
        this.releaseYear = releaseYear.substring(0, 4); // convert full date into just the year
    }

    public void setBackdrop_path(String backdrop_path) {
        // e.g. https://image.tmdb.org/t/p/w500//vQGo5VjJcHxpzIa8lMBFzpAth1w.jpg
        String tmdbUrl = "https://image.tmdb.org/t/p/w500" + backdrop_path;
        this.backdrop_path = tmdbUrl;
    }

    public void setPoster_path(String poster_path) {
        String tmdbUrl = "https://image.tmdb.org/t/p/w500" + poster_path;
        this.poster_path = tmdbUrl;
    }

}
