package com.goggin.movielist.service;

import com.goggin.movielist.model.Genre;
import com.goggin.movielist.respositories.GenreRepository;
import com.goggin.movielist.respositories.MovieConnectionRepository;
import com.goggin.movielist.respositories.MovieRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class GenreService {

    private final GenreRepository genreRepository;

    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    public Genre findOrCreateGenre(Genre genre) {
        log.info("Finding or creating genre: {}", genre);
        // Check if the genre already exists by its ID
        Optional<Genre> optionalGenre = genreRepository.findById(genre.getId());

        // If genre exists, return it
        if (optionalGenre.isPresent()) {
            return optionalGenre.get();
        }

        // If genre doesn't exist, create and save a new one
        Genre newGenre = new Genre(genre.getId(), genre.getName());
        return genreRepository.save(newGenre);
    }}
