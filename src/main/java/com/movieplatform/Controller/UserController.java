package com.movieplatform.Controller;

import com.movieplatform.Entity.User;
import com.movieplatform.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RequestMapping(path = "user")
@RestController
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<User> getall(){
        return userRepository.findAll();
    }

    @PostMapping("/login")
    public User login(@RequestBody User user){
        // 1. Check what Java received from JS
        System.out.println("JAVA RECEIVED - Email: " + user.getGmail());
        System.out.println("JAVA RECEIVED - Password: " + user.getPassword());

        User check = userRepository.findByGmail(user.getGmail());

        // 2. Check what Java found in the Database
        if(check != null) {
            System.out.println("DB FOUND - User: " + check.getName() + " | Password: " + check.getPassword());
        } else {
            System.out.println("DB FOUND - Absolutely nothing (null) for email: " + user.getGmail());
        }

        if(check != null){
            if(check.getPassword().equals(user.getPassword())){
                return check;
            } else {
                System.out.println("FAILED - Passwords did not match!");
                return null;
            }
        } else {
            return null;
        }
    }

    // create new user with email validation
    @PostMapping("/register")
    public ResponseEntity<?> create(@RequestBody User user) {
        // Check if email already exists
        User existingUser = userRepository.findByGmail(user.getGmail());

        if(existingUser != null) {
            // Email already exists - return error response
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Email already registered");
            errorResponse.put("message", "An account with this email already exists");

            System.out.println("REGISTRATION FAILED - Email already exists: " + user.getGmail());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }

        // Email is unique - proceed with registration
        user.setAdmin(0);
        User savedUser = userRepository.save(user);
        System.out.println("REGISTRATION SUCCESS - New user created: " + savedUser.getGmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }


    // get single user by id
    @GetMapping("/{id}")
    public User getById(@PathVariable Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("user not found with id: " + id));
    }



    // update existing user with email validation
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody User userDetails) {
        // first check if user exists
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("user not found"));

        // Check if email is being changed to one that already exists
        if(!user.getGmail().equals(userDetails.getGmail())) {
            User existingUser = userRepository.findByGmail(userDetails.getGmail());
            if(existingUser != null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Email already in use");
                errorResponse.put("message", "This email is already registered to another account");

                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            }
        }

        // update the fields
        user.setName(userDetails.getName());
        user.setPassword(userDetails.getPassword());
        user.setGmail(userDetails.getGmail());
        user.setAdmin(userDetails.getAdmin());

        // save n return
        User updatedUser = userRepository.save(user);
        return ResponseEntity.ok(updatedUser);
    }

    // delete user
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Integer id) {
        // check if exists first
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("user doesnt exist");
        }

        userRepository.deleteById(id);
        return "user deleted successfully";
    }
}