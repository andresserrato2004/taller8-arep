package com.example.stream.service;

import com.example.stream.dto.PostRequest;
import com.example.stream.dto.PostResponse;
import com.example.stream.model.Post;
import com.example.stream.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {
    
    @Autowired
    private PostRepository postRepository;
    
    @Transactional
    public PostResponse createPost(PostRequest request, String userId, String username) {
        // Validar que el contenido no exceda 140 caracteres
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("El contenido del post no puede estar vacío");
        }
        
        if (request.getContent().length() > 140) {
            throw new IllegalArgumentException("El post no puede exceder 140 caracteres");
        }
        
        Post post = new Post();
        post.setContent(request.getContent());
        post.setUserId(userId);
        post.setUsername(username);
        
        Post savedPost = postRepository.save(post);
        
        return mapToResponse(savedPost);
    }
    
    @Transactional(readOnly = true)
    public List<PostResponse> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<PostResponse> getPostsByUser(String userId) {
        return postRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public PostResponse getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post no encontrado"));
        return mapToResponse(post);
    }
    
    @Transactional
    public void deletePost(Long id, String userId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post no encontrado"));
        
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("No tienes permiso para eliminar este post");
        }
        
        postRepository.delete(post);
    }
    
    private PostResponse mapToResponse(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .content(post.getContent())
                .userId(post.getUserId())
                .username(post.getUsername())
                .createdAt(post.getCreatedAt())
                .build();
    }
}
