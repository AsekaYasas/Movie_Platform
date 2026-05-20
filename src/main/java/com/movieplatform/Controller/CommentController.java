package com.movieplatform.Controller;

import com.movieplatform.Entity.Comment;
import com.movieplatform.Entity.Movie;
import com.movieplatform.Entity.User;
import com.movieplatform.Repository.CommentRepository;
import com.movieplatform.Repository.MovieRepository;
import com.movieplatform.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = "*")
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MovieRepository movieRepository;

    // ── GET COMMENTS ──
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<Map<String, Object>>> getMovieComments(@PathVariable Integer movieId) {
        List<Comment> rawComments = commentRepository.findByMovie_IdOrderByCreateAtDesc(movieId);

        List<Map<String, Object>> styledComments = rawComments.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            String fullName = c.getUser() != null ? c.getUser().getName() : "Guest";
            if (fullName == null || fullName.trim().isEmpty()) fullName = "Guest";

            String initials = fullName.length() >= 2 ? fullName.substring(0, 2).toUpperCase() : fullName.toUpperCase();

            String[] colors = {"#4c1d95", "#065f46", "#7c2d12", "#2d1b69", "#1e3a8a", "#0f766e"};
            int colorIndex = Math.abs(fullName.hashCode()) % colors.length;

            map.put("id", c.getId());
            map.put("name", fullName);
            map.put("initials", initials);
            map.put("color", colors[colorIndex]);
            map.put("text", c.getBody());
            map.put("time", c.getCreateAt() != null ? c.getCreateAt().toString() : "Recent");

            map.put("likes", 0);
            map.put("dislikes", 0);
            return map;
        }).toList();

        return ResponseEntity.ok(styledComments);
    }

    // ── POST COMMENT (Cleaned up version) ──
    @PostMapping
    public ResponseEntity<?> postComment(@RequestBody Map<String, Object> payload) {
        try {
            Integer userId = payload.get("userId") != null ? Integer.valueOf(payload.get("userId").toString()) : null;
            Integer movieId = payload.get("movieId") != null ? Integer.valueOf(payload.get("movieId").toString()) : null;
            String text = (String) payload.get("body");
//comment is empty  or contains only spaces
            if (text == null || text.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Comment text cannot be empty"));
            }


            //checks wheather the user actually exists in the database
            User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
            Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new RuntimeException("Movie not found"));

            Comment comment = new Comment();
            comment.setUser(user);
            comment.setMovie(movie);
            comment.setBody(text);

            // 🌟 Cleaner: Hibernate handles generating the ID automatically now!
            Comment savedComment = commentRepository.save(comment);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedComment);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to save comment: " + e.getMessage()));
        }
    }

    // ── UPDATE AN EXISTING COMMENT
    @PutMapping("/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable Integer commentId,
            @RequestBody Map<String, Object> payload) {
        try {
            Integer userId = payload.get("userId") != null ? Integer.valueOf(payload.get("userId").toString()) : null;
            String updatedText = (String) payload.get("body");

            if (updatedText == null || updatedText.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Comment text cannot be empty"));
            }

            // 1. Locate the target comment record
            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new RuntimeException("Comment not found"));

            // 2. Security Check: Verify the requester owns this comment
            if (!comment.getUser().getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Unauthorized to edit this comment"));
            }

            // 3. Mutate the body property text fields
            comment.setBody(updatedText);

            // 4. Commit changes (Hibernate automatically overwrites the update_at timestamp here!)
            Comment savedComment = commentRepository.save(comment);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "id", savedComment.getId(),
                    "body", savedComment.getBody()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update comment: " + e.getMessage()));
        }
    }

    // ── DELETE COMMENT ──
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Integer commentId, @RequestParam Integer userId) {
        try {
            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new RuntimeException("Comment not found"));


            if (!comment.getUser().getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Unauthorized"));
            }

            commentRepository.delete(comment);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}