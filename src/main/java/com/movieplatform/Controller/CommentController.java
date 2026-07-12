package com.movieplatform.Controller;

import com.movieplatform.Service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = "*")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<Map<String, Object>>> getMovieComments(@PathVariable Integer movieId) {
        return commentService.getMovieComments(movieId);
    }

    @PostMapping
    public ResponseEntity<?> postComment(@RequestBody Map<String, Object> payload) {
        try {
            Integer userId = payload.get("userId") != null ? Integer.valueOf(payload.get("userId").toString()) : null;
            Integer movieId = payload.get("movieId") != null ? Integer.valueOf(payload.get("movieId").toString()) : null;
            String text = (String) payload.get("body");
            return commentService.postComment(userId, movieId, text);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to save comment: " + e.getMessage()));
        }
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable Integer commentId,
            @RequestBody Map<String, Object> payload) {
        try {
            Integer userId = payload.get("userId") != null ? Integer.valueOf(payload.get("userId").toString()) : null;
            String updatedText = (String) payload.get("body");
            return commentService.updateComment(commentId, userId, updatedText);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to update comment: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Integer commentId, @RequestParam Integer userId) {
        try {
            return commentService.deleteComment(commentId, userId);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}