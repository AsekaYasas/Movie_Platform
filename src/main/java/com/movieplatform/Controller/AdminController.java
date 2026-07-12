package com.movieplatform.Controller;

import com.movieplatform.Entity.Movie;
import com.movieplatform.Service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/movies")
    public ResponseEntity<?> addMovie(
            @RequestBody Movie movie,
            @RequestHeader(value = "X-User-Id", required = false) Integer requesterId) {
        return adminService.addMovie(movie, requesterId);
    }

    @DeleteMapping("/movies/{id}")
    public ResponseEntity<?> deleteMovie(
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-Id", required = false) Integer requesterId) {
        return adminService.deleteMovie(id, requesterId);
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(
            @RequestHeader(value = "X-User-Id", required = false) Integer requesterId) {
        return adminService.getAllUsers(requesterId);
    }

    @PutMapping("/users/{id}/promote")
    public ResponseEntity<?> promoteToAdmin(
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-Id", required = false) Integer requesterId) {
        return adminService.promoteToAdmin(id, requesterId);
    }

    @PutMapping("/users/{id}/demote")
    public ResponseEntity<?> demoteToUser(
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-Id", required = false) Integer requesterId) {
        return adminService.demoteToUser(id, requesterId);
    }
}