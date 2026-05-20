package com.movieplatform.Service;

import com.movieplatform.Entity.User;
import com.movieplatform.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User login(String gmail, String password) {
        User found = userRepository.findByGmail(gmail);
        if (found != null && found.getPassword().equals(password)) {
            return found;
        }
        return null;
    }

    public ResponseEntity<?> register(User user) {
        User existing = userRepository.findByGmail(user.getGmail());
        if (existing != null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Email already registered");
            error.put("message", "An account with this email already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }
        user.setAdmin(0);
        User saved = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    public User getById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public ResponseEntity<?> update(Integer id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getGmail().equals(userDetails.getGmail())) {
            User existing = userRepository.findByGmail(userDetails.getGmail());
            if (existing != null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Email already in use");
                error.put("message", "This email is already registered to another account");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }
        }

        user.setName(userDetails.getName());
        user.setPassword(userDetails.getPassword());
        user.setGmail(userDetails.getGmail());
        user.setAdmin(userDetails.getAdmin());

        return ResponseEntity.ok(userRepository.save(user));
    }

    public String delete(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User doesn't exist");
        }
        userRepository.deleteById(id);
        return "User deleted successfully";
    }
}