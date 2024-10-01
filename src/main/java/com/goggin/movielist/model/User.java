package com.goggin.movielist.model;

import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "users")
@ToString
@Data
@NoArgsConstructor
public class User {

    @Id // used to mark it as the primary key of the table
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "user_id")
    private Integer user_id; // only integer as takes less storage and am not anticipating loads of accounts
    private String email;

    private String username;

    private String password;

    private double latitude;
    private double longitude;

    private String favouriteReleaseYear;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<MovieConnection> favouriteMovies;

    public User(Integer user_id, String email, String username, String password, double latitude, double longitude) {
        this.user_id = user_id;
        this.email = email;
        this.username = username;
        this.password = password;
        this.latitude = latitude;
        this.longitude = longitude;
    }

}
