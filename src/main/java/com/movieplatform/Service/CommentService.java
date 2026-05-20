package com.movieplatform.Service;

import com.movieplatform.Entity.Comment;
import com.movieplatform.Entity.Movie;
import com.movieplatform.Entity.User;
import com.movieplatform.Repository.CommentRepository;
import com.movieplatform.Repository.MovieRepository;
import com.movieplatform.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MovieRepository movieRepository;

    private static final String[] AVATAR_COLORS = {
            "#4c1d95", "#065f46", "#7c2d12", "#2d1b69", "#1e3a8a", "#0f766e"
    };

    public ResponseEntity<List<Map<String, Object>>> getMovieComments(Integer movieId) {
        List<Comment> raw = commentRepository.findByMovie_IdOrderByCreateAtDesc(movieId);

        List<Map<String, Object>> styled = raw.stream().map(c -> {
            String fullName = (c.getUser() != null && c.getUser().getName() != null
                    && !c.getUser().getName().trim().isEmpty())
                    ? c.getUser().getName() : "Guest";

            String initials = fullName.length() >= 2
                    ? fullName.substring(0, 2).toUpperCase()
                    : fullName.toUpperCase();

            int colorIndex = Math.abs(fullName.hashCode()) % AVATAR_COLORS.length;

            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("name", fullName);
            map.put("initials", initials);
            map.put("color", AVATAR_COLORS[colorIndex]);
            map.put("text", c.getBody());
            map.put("time", c.getCreateAt() != null ? c.getCreateAt().toString() : "Recent");
            map.put("likes", 0);
            map.put("dislikes", 0);
            return map;
        }).toList();

        return ResponseEntity.ok(styled);
    }

    public ResponseEntity<?> postComment(Integer userId, Integer movieId, String text) {
        if (text == null || text.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Comment text cannot be empty"));
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new RuntimeException("Movie not found"));

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setMovie(movie);
        comment.setBody(text);

        return ResponseEntity.status(HttpStatus.CREATED).body(commentRepository.save(comment));
    }

    public ResponseEntity<?> updateComment(Integer commentId, Integer userId, String updatedText) {
        if (updatedText == null || updatedText.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Comment text cannot be empty"));
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Unauthorized to edit this comment"));
        }

        comment.setBody(updatedText);
        Comment saved = commentRepository.save(comment);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "id", saved.getId(),
                "body", saved.getBody()
        ));
    }

    public ResponseEntity<?> deleteComment(Integer commentId, Integer userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Unauthorized"));
        }

        commentRepository.delete(comment);
        return ResponseEntity.ok(Map.of("success", true));
    }
}