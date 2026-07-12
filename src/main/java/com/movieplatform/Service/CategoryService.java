package com.movieplatform.Service;

import com.movieplatform.Entity.Category;
import com.movieplatform.Entity.CategoryHasMovie;
import com.movieplatform.Entity.CategoryHasMovieId;
import com.movieplatform.Entity.Movie;
import com.movieplatform.Repository.CategoryHasMovieRepository;
import com.movieplatform.Repository.CategoryRepository;
import com.movieplatform.Repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryHasMovieRepository categoryHasMovieRepository;

    @Autowired
    private MovieRepository movieRepository;

    // ── BASIC CATEGORY CRUD ──

    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    public Category create(Category category) {
        return categoryRepository.save(category);
    }

    public ResponseEntity<?> delete(Integer id) {
        if (!categoryRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Category not found"));
        }
        categoryRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Category deleted"));
    }

    // ── MANY-TO-MANY ASSIGNMENT ──

    // Assign a movie to a category
    public ResponseEntity<?> assignMovie(Integer categoryId, Integer movieId) {
        // Validate both sides exist
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + movieId));

        // Duplicate guard
        Optional<CategoryHasMovie> existing = categoryHasMovieRepository
                .findById_CategoryIdAndId_MovieId(categoryId, movieId);
        if (existing.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "This movie is already assigned to this category"));
        }

        CategoryHasMovieId id = new CategoryHasMovieId();
        id.setCategoryId(categoryId);
        id.setMovieId(movieId);

        CategoryHasMovie link = new CategoryHasMovie();
        link.setId(id);
        link.setCategory(category);
        link.setMovie(movie);

        categoryHasMovieRepository.save(link);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Movie assigned to category successfully"));
    }

    // Remove a movie from a category
    public ResponseEntity<?> removeMovie(Integer categoryId, Integer movieId) {
        CategoryHasMovie link = categoryHasMovieRepository
                .findById_CategoryIdAndId_MovieId(categoryId, movieId)
                .orElse(null);

        if (link == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No such assignment exists"));
        }

        categoryHasMovieRepository.delete(link);
        return ResponseEntity.ok(Map.of("message", "Movie removed from category successfully"));
    }

    // Get all movies in a category
    public ResponseEntity<?> getMoviesByCategory(Integer categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Category not found"));
        }

        List<Movie> movies = categoryHasMovieRepository.findById_CategoryId(categoryId)
                .stream()
                .map(CategoryHasMovie::getMovie)
                .toList();

        return ResponseEntity.ok(movies);
    }

    // Get all categories for a movie
    public ResponseEntity<?> getCategoriesByMovie(Integer movieId) {
        if (!movieRepository.existsById(movieId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Movie not found"));
        }

        List<Category> categories = categoryHasMovieRepository.findById_MovieId(movieId)
                .stream()
                .map(CategoryHasMovie::getCategory)
                .toList();

        return ResponseEntity.ok(categories);
    }
}