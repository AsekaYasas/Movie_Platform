package com.movieplatform.Service;

import com.movieplatform.Entity.Movie;
import com.movieplatform.Entity.Review;
import com.movieplatform.Entity.User;
import com.movieplatform.Repository.MovieRepository;
import com.movieplatform.Repository.RatingReviewRepository;
import com.movieplatform.Repository.UserRepository;
import com.movieplatform.Util.RatingUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RatingReviewService {

    @Autowired
    private RatingReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MovieRepository movieRepository;

    public Map<String, Object> getMovieRatingStats(Integer movieId, Integer userId) {
        Double avgRating = reviewRepository.getAverageRating(movieId);
        Long totalRatings = reviewRepository.countRatings(movieId);
        List<Review> movieReviews = reviewRepository.findByMovies_Id(movieId);

        long totalLikes = movieReviews.stream().mapToLong(r -> r.getLikes() != null ? r.getLikes() : 0).sum();
        long totalDislikes = movieReviews.stream().mapToLong(r -> r.getDislikes() != null ? r.getDislikes() : 0).sum();

        boolean userLiked = false;
        boolean userDisliked = false;

        if (userId != null) {
            Optional<Review> userReview = reviewRepository.findByUsers_IdAndMovies_Id(userId, movieId);
            if (userReview.isPresent()) {
                userLiked = userReview.get().getLikes() != null && userReview.get().getLikes() == 1;
                userDisliked = userReview.get().getDislikes() != null && userReview.get().getDislikes() == 1;
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("averageRating", RatingUtil.formatAverageRating(avgRating));
        stats.put("averageRaw", avgRating != null ? avgRating : 0.0);
        stats.put("totalRatings", totalRatings);
        stats.put("ratingText", RatingUtil.getRatingCountText(totalRatings));
        stats.put("likes", totalLikes);
        stats.put("dislikes", totalDislikes);
        stats.put("userLikesState", userLiked);
        stats.put("userDislikesState", userDisliked);
        return stats;
    }

    public Map<String, Object> getUserRating(Integer userId, Integer movieId) {
        Optional<Review> existing = reviewRepository.findByUsers_IdAndMovies_Id(userId, movieId);
        boolean hasRated = existing.isPresent() && existing.get().getUserRating() != null;

        Map<String, Object> response = new HashMap<>();
        response.put("hasRated", hasRated);
        response.put("userRating", hasRated ? existing.get().getUserRating() : null);
        return response;
    }

    public ResponseEntity<Map<String, Object>> submitRating(Integer userId, Integer movieId, Integer rating) {
        if (!RatingUtil.isValidRating(rating)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Rating must be between 1 and 5"));
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new RuntimeException("Movie not found"));

        Review review = reviewRepository.findByUsers_IdAndMovies_Id(userId, movieId).orElse(new Review());
        review.setUsers(user);
        review.setMovies(movie);
        review.setUserRating(rating);
        reviewRepository.save(review);

        Double newAvg = reviewRepository.getAverageRating(movieId);
        Long totalRatings = reviewRepository.countRatings(movieId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "userRating", rating,
                "newAverage", RatingUtil.formatAverageRating(newAvg),
                "totalRatings", totalRatings
        ));
    }

    public ResponseEntity<Map<String, Object>> vote(Integer movieId, Integer userId, String voteType) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new RuntimeException("Movie not found"));

        Review review = reviewRepository.findByUsers_IdAndMovies_Id(userId, movieId).orElse(new Review());
        review.setUsers(user);
        review.setMovies(movie);

        int currentLikes = review.getLikes() != null ? review.getLikes() : 0;
        int currentDislikes = review.getDislikes() != null ? review.getDislikes() : 0;

        if ("like".equals(voteType)) {
            review.setLikes(currentLikes == 1 ? 0 : 1);
            if (currentLikes != 1) review.setDislikes(0);
        } else if ("dislike".equals(voteType)) {
            review.setDislikes(currentDislikes == 1 ? 0 : 1);
            if (currentDislikes != 1) review.setLikes(0);
        }

        reviewRepository.save(review);

        List<Review> updated = reviewRepository.findByMovies_Id(movieId);
        long globalLikes = updated.stream().mapToLong(r -> r.getLikes() != null ? r.getLikes() : 0).sum();
        long globalDislikes = updated.stream().mapToLong(r -> r.getDislikes() != null ? r.getDislikes() : 0).sum();

        return ResponseEntity.ok(Map.of(
                "likes", globalLikes,
                "dislikes", globalDislikes,
                "userLikesState", review.getLikes() != null && review.getLikes() == 1,
                "userDislikesState", review.getDislikes() != null && review.getDislikes() == 1
        ));
    }
}