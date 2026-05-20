package com.movieplatform.Util;

import com.movieplatform.Entity.Rental;
import com.movieplatform.Repository.RentalRepository;

import java.time.Instant;
import java.util.Optional;

public class RentalUtil {

    public static boolean hasActiveRental(RentalRepository rentalRepository, Integer userId, Integer movieId) {
        if (rentalRepository == null || userId == null || movieId == null) {
            return false;
        }

        // Run our explicit parameterized query safely
        Optional<Rental> latestRental = rentalRepository.findTopByUsersIdAndMoviesIdOrderByIdDesc(userId, movieId);

        if (latestRental.isEmpty()) {
            return false;
        }

        Instant now = Instant.now();
        return now.isBefore(latestRental.get().getExpiryDate());
    }
}