package com.movieplatform.Repository;

import com.movieplatform.Entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface RatingReviewRepository extends JpaRepository<Review, Integer> {

    // 🌟 CHANGE THIS: Remove the 'OrderBy...' part since there are no timestamps here
    List<Review> findByMovies_Id(Integer movieId);

    Optional<Review> findByUsers_IdAndMovies_Id(Integer userId, Integer movieId);

    @Query("SELECT AVG(r.userRating) FROM Review r WHERE r.movies.id = :movieId")
    Double getAverageRating(@Param("movieId") Integer movieId);

    @Query("SELECT COUNT(r.userRating) FROM Review r WHERE r.movies.id = :movieId AND r.userRating IS NOT NULL")
    Long countRatings(@Param("movieId") Integer movieId);
}