package com.goggin.movielist.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Genre {

    @Id
    private Integer id; // id pulled from tmdb json
    private String name; // name pulled from tmdb json
}
