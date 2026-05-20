package com.movieplatform.Controller;

import com.movieplatform.Entity.Movie;
import com.movieplatform.Entity.Review;
import com.movieplatform.Entity.User;
import com.movieplatform.Repository.MovieRepository;
import com.movieplatform.Repository.RatingReviewRepository;
import com.movieplatform.Repository.UserRepository;
import com.movieplatform.Util.RatingUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/rating")
@CrossOrigin(origins = "*")
public class RatingReviewController {

    @Autowired
    private RatingReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MovieRepository movieRepository;

    // ── GET MOVIE RATING STATS + USER HISTORICAL STATE ON LOAD ──
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<Map<String, Object>> getMovieRatingStats(
            @PathVariable Integer movieId,
            @RequestParam(required = false) Integer userId) {

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

        return ResponseEntity.ok(stats);
    }

    // Checks if a specific logged-in user has already given this movie a star rating
    @GetMapping("/user/{userId}/movie/{movieId}")
    public ResponseEntity<Map<String, Object>> getUserRating(
            @PathVariable Integer userId,
            @PathVariable Integer movieId) {

        Optional<Review> existingReview = reviewRepository.findByUsers_IdAndMovies_Id(userId, movieId);

        Map<String, Object> response = new HashMap<>();
        boolean hasRated = existingReview.isPresent() && existingReview.get().getUserRating() != null;

        response.put("hasRated", hasRated);
        response.put("userRating", hasRated ? existingReview.get().getUserRating() : null);

        return ResponseEntity.ok(response);
    }

    // Saves or updates a user's 1-5 star rating
    @PostMapping("/rate")
    public ResponseEntity<Map<String, Object>> submitRating(@RequestBody Map<String, Integer> payload) {
        Integer userId = payload.get("userId");
        Integer movieId = payload.get("movieId");
        Integer rating = payload.get("rating");

        if (!RatingUtil.isValidRating(rating)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Rating must be between 1 and 5"));
        }
     //Checks whether the user exists in the database .That prevent from non ratings
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new RuntimeException("Movie not found"));

        Review review = reviewRepository.findByUsers_IdAndMovies_Id(userId, movieId).orElse(new Review());

        review.setUsers(user);
        review.setMovies(movie);
        review.setUserRating(rating);
        reviewRepository.save(review);

        Double newAvg = reviewRepository.getAverageRating(movieId);
        Long totalRatings = reviewRepository.countRatings(movieId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("userRating", rating);
        response.put("newAverage", RatingUtil.formatAverageRating(newAvg));
        response.put("totalRatings", totalRatings);

        return ResponseEntity.ok(response);
    }

    // Handles strict 1-vote-per-user logic. Prevents double-voting and stops simultaneous like/dislike.
    @PostMapping("/movie/{movieId}/vote")
    public ResponseEntity<Map<String, Object>> voteOnMovie(
            @PathVariable Integer movieId,
            @RequestBody Map<String, Object> payload) {

        Integer userId = (Integer) payload.get("userId");
        String voteType = ((String) payload.get("type")).toLowerCase();

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new RuntimeException("Movie not found"));

        Review review = reviewRepository.findByUsers_IdAndMovies_Id(userId, movieId).orElse(new Review());

        review.setUsers(user);
        review.setMovies(movie);

        int currentLikes = review.getLikes() != null ? review.getLikes() : 0;
        int currentDislikes = review.getDislikes() != null ? review.getDislikes() : 0;

        if ("like".equals(voteType)) {
            if (currentLikes == 1) {
                review.setLikes(0);
            } else {
                review.setLikes(1);
                review.setDislikes(0);
            }
        } else if ("dislike".equals(voteType)) {
            if (currentDislikes == 1) {
                review.setDislikes(0);
            } else {
                review.setDislikes(1);
                review.setLikes(0);
            }
        }

        reviewRepository.save(review);

        List<Review> updatedReviews = reviewRepository.findByMovies_Id(movieId);
        long globalLikes = updatedReviews.stream().mapToLong(r -> r.getLikes() != null ? r.getLikes() : 0).sum();
        long globalDislikes = updatedReviews.stream().mapToLong(r -> r.getDislikes() != null ? r.getDislikes() : 0).sum();

        return ResponseEntity.ok(Map.of(
                "likes", globalLikes,
                "dislikes", globalDislikes,
                "userLikesState", review.getLikes() != null && review.getLikes() == 1,
                "userDislikesState", review.getDislikes() != null && review.getDislikes() == 1
        ));
    }
}