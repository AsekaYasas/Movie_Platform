package com.movieplatform.Controller;

import com.movieplatform.Entity.Rental;
import com.movieplatform.Repository.RentalRepository;
import com.movieplatform.Util.RentalUtil; // Import your new static utility class!
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RequestMapping(path = "rental")
@RestController
public class RentalController {

    @Autowired
    private RentalRepository rentalRepository;

    @GetMapping
    public List<Rental> getall(){
        return rentalRepository.findAll();
    }

    // ── POST: CREATES RENTAL WITH UTILITY TRANSACTION PROTECTION ──
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Rental rental) {
        if (rental.getUsers() == null || rental.getMovies() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing User or Movie reference."));
        }

        Integer userId = rental.getUsers().getId();
        Integer movieId = rental.getMovies().getId();

        // 🛑 CALL STATIC UTILITY BLOCKER: Checks database for an active token pass
        if (RentalUtil.hasActiveRental(rentalRepository, userId, movieId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "You already have an active rental pass for this movie!"));
        }

        // If validation clears, timestamp and persist the entity
        Instant now = Instant.now();
        rental.setRentalDate(now);
        rental.setExpiryDate(now.plus(2, ChronoUnit.DAYS)); // 48-Hour access window

        Rental saved = rentalRepository.save(rental);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ── GET: GATEKEEPER SERVICE ENDPOINT ──
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> hasRented(@RequestParam Integer userId, @RequestParam Integer movieId) {
        // 🌟 Execute the exact same shared utility code block statically
        boolean isValid = RentalUtil.hasActiveRental(rentalRepository, userId, movieId);
        return ResponseEntity.ok(Map.of("hasActiveRental", isValid));
    }
}