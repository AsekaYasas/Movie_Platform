package com.movieplatform.Controller;

import com.movieplatform.Entity.Movie;
import com.movieplatform.Entity.User;
import com.movieplatform.Repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin
@RequestMapping(path = "movies")
@RestController
public class MovieController {

    @Autowired
    private MovieRepository movieRepository;

    @GetMapping
    public List<Movie> getall(){
        return movieRepository.findAll();
    }
    // get single movie by id
    @GetMapping("/{id}")
    public Movie getById(@PathVariable Integer id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));
    }

    // add new movie
    @PostMapping
    public Movie create(@RequestBody Movie movie) {
        // just save it, jpa handles everything
        return movieRepository.save(movie);
    }

    @PutMapping("/{id}")
    public Movie updateMovie(@PathVariable Integer id, @RequestBody Movie movieDetails) {
        // 1. Find the existing movie
        Movie existingMovie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));

        // 2. Update the fields
        existingMovie.setName(movieDetails.getName());
        existingMovie.setLanguage(movieDetails.getLanguage());
        existingMovie.setCountry(movieDetails.getCountry());
        existingMovie.setShortdescription(movieDetails.getShortdescription());

        // ─── ADDED THESE MISSING FIELDS ───
        existingMovie.setDescription(movieDetails.getDescription());
        existingMovie.setTrailerlink(movieDetails.getTrailerlink());
        existingMovie.setTomato(movieDetails.getTomato());
        // ──────────────────────────────────

        existingMovie.setPrice(movieDetails.getPrice());
        existingMovie.setImage(movieDetails.getImage());
        existingMovie.setLink(movieDetails.getLink());
        existingMovie.setImdb(movieDetails.getImdb());

        // IMPORTANT: Handle the category update
        if (movieDetails.getCategory() != null) {
            existingMovie.setCategory(movieDetails.getCategory());
        }

        // 3. Save it back to the DB
        return movieRepository.save(existingMovie);
    }

    // delete movie
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Integer id) {
        // check if exists first
        if (!movieRepository.existsById(id)) {
            throw new RuntimeException("Movie doesnt exist");
        }

        movieRepository.deleteById(id);
        return "Movie deleted successfully";
    }
}
