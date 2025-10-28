package com.example.stream.repository;

import com.example.stream.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUserIdOrderByCreatedAtDesc(String userId);
    List<Post> findAllByOrderByCreatedAtDesc();
}
