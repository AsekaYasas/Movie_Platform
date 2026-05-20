package com.movieplatform.Controller;

import com.movieplatform.Entity.Movie;
import com.movieplatform.Service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RequestMapping(path = "movies")
@RestController
public class MovieController {

    @Autowired
    private MovieService movieService;

    @GetMapping
    public List<Movie> getAll() {
        return movieService.getAll();
    }

    @GetMapping("/{id}")
    public Movie getById(@PathVariable Integer id) {
        return movieService.getById(id);
    }

    @PostMapping
    public Movie create(@RequestBody Movie movie) {
        return movieService.create(movie);
    }

    @PutMapping("/{id}")
    public Movie update(@PathVariable Integer id, @RequestBody Movie movieDetails) {
        return movieService.update(id, movieDetails);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Integer id) {
        return movieService.delete(id);
    }
}