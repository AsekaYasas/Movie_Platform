package com.movieplatform.Service;

import com.movieplatform.Entity.Payment;
import com.movieplatform.Entity.Rental;
import com.movieplatform.Repository.PaymentRepository;
import com.movieplatform.Repository.RentalRepository;
import com.movieplatform.Util.RentalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private RentalService rentalService;

    @Transactional
    public ResponseEntity<?> processPayment(Payment payment) {
        // 1. Structural Validation
        if (payment.getUsers() == null || payment.getMovie() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing User or Movie reference."));
        }

        Integer userId = payment.getUsers().getId();
        Integer movieId = payment.getMovie().getId();

        // 2. Double-Renting Validation
        if (RentalUtil.hasActiveRental(rentalRepository, userId, movieId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "You already have an active pass for this movie!"));
        }

        // 3. Mock Payment Processing Loop
        boolean paymentSuccessful = true;

        if (paymentSuccessful) {
            // Create entity container
            Rental newRental = new Rental();
            newRental.setUsers(payment.getUsers());
            newRental.setMovies(payment.getMovie());

            // Delegate core tier duration calculations to RentalService
            Rental savedRental = rentalService.createRentalRecord(newRental);

            // Link records 1:1 and save transaction log
            payment.setRentals(savedRental);
            paymentRepository.save(payment);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "status", "Payment Successful!",
                    "rentalExpiry", savedRental.getExpiryDate()
            ));
        }

        // 4. Default Fallback
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                .body(Map.of("error", "Transaction declined."));
    }
}