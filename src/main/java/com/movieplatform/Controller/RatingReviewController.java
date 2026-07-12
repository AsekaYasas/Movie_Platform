package com.movieplatform.Controller;

import com.movieplatform.Service.RatingReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/rating")
@CrossOrigin(origins = "*")
public class RatingReviewController {

    @Autowired
    private RatingReviewService ratingReviewService;

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<Map<String, Object>> getMovieRatingStats(
            @PathVariable Integer movieId,
            @RequestParam(required = false) Integer userId) {
        return ResponseEntity.ok(ratingReviewService.getMovieRatingStats(movieId, userId));
    }

    @GetMapping("/user/{userId}/movie/{movieId}")
    public ResponseEntity<Map<String, Object>> getUserRating(
            @PathVariable Integer userId,
            @PathVariable Integer movieId) {
        return ResponseEntity.ok(ratingReviewService.getUserRating(userId, movieId));
    }

    @PostMapping("/rate")
    public ResponseEntity<Map<String, Object>> submitRating(@RequestBody Map<String, Integer> payload) {
        return ratingReviewService.submitRating(
                payload.get("userId"),
                payload.get("movieId"),
                payload.get("rating")
        );
    }

    @PostMapping("/movie/{movieId}/vote")
    public ResponseEntity<Map<String, Object>> vote(
            @PathVariable Integer movieId,
            @RequestBody Map<String, Object> payload) {
        Integer userId = (Integer) payload.get("userId");
        String voteType = ((String) payload.get("type")).toLowerCase();
        return ratingReviewService.vote(movieId, userId, voteType);
    }
}