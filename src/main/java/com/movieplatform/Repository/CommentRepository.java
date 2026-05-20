package com.movieplatform.Repository;

import com.movieplatform.Entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findByMovie_IdOrderByCreateAtDesc(Integer movieId);
}