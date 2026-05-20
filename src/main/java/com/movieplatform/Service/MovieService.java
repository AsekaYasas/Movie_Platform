package com.movieplatform.Service;

import com.movieplatform.Entity.Movie;
import com.movieplatform.Repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;

    public List<Movie> getAll() {
        return movieRepository.findAll();
    }

    public Movie getById(Integer id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));
    }

    public Movie create(Movie movie) {
        return movieRepository.save(movie);
    }

    public Movie update(Integer id, Movie movieDetails) {
        Movie existing = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));

        existing.setName(movieDetails.getName());
        existing.setLanguage(movieDetails.getLanguage());
        existing.setCountry(movieDetails.getCountry());
        existing.setShortdescription(movieDetails.getShortdescription());
        existing.setDescription(movieDetails.getDescription());
        existing.setTrailerlink(movieDetails.getTrailerlink());
        existing.setTomato(movieDetails.getTomato());
        existing.setPrice(movieDetails.getPrice());
        existing.setImage(movieDetails.getImage());
        existing.setLink(movieDetails.getLink());
        existing.setImdb(movieDetails.getImdb());

        if (movieDetails.getCategory() != null) {
            existing.setCategory(movieDetails.getCategory());
        }

        return movieRepository.save(existing);
    }

    public String delete(Integer id) {
        if (!movieRepository.existsById(id)) {
            throw new RuntimeException("Movie doesn't exist");
        }
        movieRepository.deleteById(id);
        return "Movie deleted successfully";
    }
}