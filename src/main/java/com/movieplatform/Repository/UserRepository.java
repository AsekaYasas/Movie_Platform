package com.movieplatform.Repository;

import com.movieplatform.Entity.User;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByGmail(String gmail);
    List<User> findByAdmin(Integer admin); //  listing by role jut in case
}
