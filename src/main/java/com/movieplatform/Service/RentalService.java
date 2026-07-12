package com.movieplatform.Service;

import com.movieplatform.Entity.Rental;
import com.movieplatform.Entity.User;
import com.movieplatform.Repository.RentalRepository;
import com.movieplatform.Repository.UserRepository;
import com.movieplatform.Util.RentalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
public class RentalService {

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Rental> getAll() {
        return rentalRepository.findAll();
    }

    public Rental createRentalRecord(Rental rental) {
        // Fetch the real User entity from DB — don't trust the stub from @RequestBody
        User user = userRepository.findById(rental.getUsers().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Instant now = Instant.now();
        Instant expiryDate = (user.getIsPremium() != null && user.getIsPremium() == 1)
                ? now.plus(30, ChronoUnit.DAYS)
                : now.plus(7, ChronoUnit.DAYS);

        rental.setRentalDate(now);
        rental.setExpiryDate(expiryDate);
        return rentalRepository.save(rental);
    }

    public ResponseEntity<?> create(Rental rental) {
        if (rental.getUsers() == null || rental.getMovies() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing User or Movie reference."));
        }

        Integer userId = rental.getUsers().getId();
        Integer movieId = rental.getMovies().getId();

        if (RentalUtil.hasActiveRental(rentalRepository, userId, movieId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "You already have an active rental pass for this movie!"));
        }

        // Delegate to createRentalRecord — single source of truth for timestamps
        return ResponseEntity.status(HttpStatus.CREATED).body(createRentalRecord(rental));
    }

    public Map<String, Object> checkActiveRental(Integer userId, Integer movieId) {
        boolean isValid = RentalUtil.hasActiveRental(rentalRepository, userId, movieId);
        return Map.of("hasActiveRental", isValid);
    }
}