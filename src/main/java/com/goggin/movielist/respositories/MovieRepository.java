package com.goggin.movielist.respositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.goggin.movielist.model.Movie;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer> {
    Movie findByTitle(String title);

    // Iterable<Movie> findAllByOrderByRatingDesc();
    /*
     * This gives all the functionality of:
     * <S extends T> S save(S entity);
     * 
     * <S extends T> Iterable<S> saveAll(Iterable<S> entities);
     * 
     * Optional<T> findById(ID id);
     * 
     * boolean existsById(ID id);
     * 
     * Iterable<T> findAll();
     * 
     * Iterable<T> findAllById(Iterable<ID> ids);
     * 
     * long count();
     * 
     * void deleteById(ID id);
     * 
     * void delete(T entity);
     * 
     * void deleteAllById(Iterable<? extends ID> ids);
     * 
     * void deleteAll(Iterable<? extends T> entities);
     * 
     * void deleteAll();
     */
}
