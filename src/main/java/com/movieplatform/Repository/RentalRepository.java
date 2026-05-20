package com.movieplatform.Repository;

import com.movieplatform.Entity.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RentalRepository extends JpaRepository<Rental, Integer> {

    List<Rental> findAllByUsers_Id(Integer id);

    void deleteById(Integer id);

    boolean existsByUsers_IdAndMovies_Id(Integer userId, Integer movieId);

    // 🌟 THE FIX: Hardcode the JPQL relationship traversal paths explicitly
    @Query("SELECT r FROM Rental r WHERE r.users.id = :userId AND r.movies.id = :movieId ORDER BY r.id DESC LIMIT 1")
    Optional<Rental> findTopByUsersIdAndMoviesIdOrderByIdDesc(
            @Param("userId") Integer userId,
            @Param("movieId") Integer movieId
    );
}