package com.movieplatform.Controller;

import com.movieplatform.Entity.Movie;
import com.movieplatform.Entity.User;
import com.movieplatform.Repository.MovieRepository;
import com.movieplatform.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired private MovieRepository movieRepository;
    @Autowired private UserRepository userRepository;

    // ── SHARED AUTH HELPERS ──────────────────────────────

    // Allows admin (1) OR superadmin (2)
    private ResponseEntity<?> checkAdmin(Integer requesterId) {
        if (requesterId == null) return ResponseEntity.status(403).body("Forbidden");
        User requester = userRepository.findById(requesterId).orElse(null);
        if (requester == null || requester.getAdmin() < 1) return ResponseEntity.status(403).body("Forbidden");
        return null;
    }

    // Superadmin (2) only
    private ResponseEntity<?> checkSuperAdmin(Integer requesterId) {
        if (requesterId == null) return ResponseEntity.status(403).body("Forbidden");
        User requester = userRepository.findById(requesterId).orElse(null);
        if (requester == null || requester.getAdmin() != 2) return ResponseEntity.status(403).body("Forbidden");
        return null;
    }

    // ── MOVIE MANAGEMENT (admin + superadmin) ────────────

    @PostMapping("/movies")
    public ResponseEntity<?> addMovie(
            @RequestBody Movie movie,
            @RequestHeader(value = "X-User-Id", required = false) Integer requesterId) {
        ResponseEntity<?> auth = checkAdmin(requesterId);
        if (auth != null) return auth;
        return ResponseEntity.ok(movieRepository.save(movie));
    }

    @DeleteMapping("/movies/{id}")
    public ResponseEntity<?> deleteMovie(
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-Id", required = false) Integer requesterId) {
        ResponseEntity<?> auth = checkAdmin(requesterId);
        if (auth != null) return auth;
        movieRepository.deleteById(id);
        return ResponseEntity.ok("Movie deleted successfully");
    }

    // ── USER MANAGEMENT ──────────────────────────────────

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(
            @RequestHeader(value = "X-User-Id", required = false) Integer requesterId) {
        ResponseEntity<?> auth = checkAdmin(requesterId);
        if (auth != null) return auth;
        return ResponseEntity.ok(userRepository.findAll());
    }

    // Superadmin only: promote user → admin
    @PutMapping("/users/{id}/promote")
    public ResponseEntity<?> promoteToAdmin(
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-Id", required = false) Integer requesterId) {
        ResponseEntity<?> auth = checkSuperAdmin(requesterId);
        if (auth != null) return auth;

        User target = userRepository.findById(id).orElse(null);
        if (target == null) return ResponseEntity.status(404).body("User not found");
        if (target.getAdmin() == 2) return ResponseEntity.badRequest().body("Cannot change a superadmin");

        target.setAdmin(1);
        userRepository.save(target);
        return ResponseEntity.ok("User promoted to admin");
    }

    // Superadmin only: demote admin → user
    @PutMapping("/users/{id}/demote")
    public ResponseEntity<?> demoteToUser(
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-Id", required = false) Integer requesterId) {
        ResponseEntity<?> auth = checkSuperAdmin(requesterId);
        if (auth != null) return auth;

        User target = userRepository.findById(id).orElse(null);
        if (target == null) return ResponseEntity.status(404).body("User not found");
        if (target.getAdmin() == 2) return ResponseEntity.badRequest().body("Cannot change a superadmin");

        target.setAdmin(0);
        userRepository.save(target);
        return ResponseEntity.ok("User demoted to regular user");
    }
}