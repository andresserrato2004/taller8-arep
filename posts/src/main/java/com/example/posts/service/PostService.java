package com.example.posts.service;

import com.example.posts.dto.PostRequest;
import com.example.posts.dto.PostResponse;
import com.example.posts.model.Post;
import com.example.posts.repository.PostRepository;
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
    
    @Transactional
    public PostResponse updatePost(Long id, PostRequest request, String userId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post no encontrado"));
        
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("No tienes permiso para actualizar este post");
        }
        
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("El contenido del post no puede estar vacío");
        }
        
        if (request.getContent().length() > 140) {
            throw new IllegalArgumentException("El post no puede exceder 140 caracteres");
        }
        
        post.setContent(request.getContent());
        Post updatedPost = postRepository.save(post);
        
        return mapToResponse(updatedPost);
    }
    
    @Transactional
    public PostResponse likePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post no encontrado"));
        
        post.setLikeCount(post.getLikeCount() + 1);
        Post updatedPost = postRepository.save(post);
        
        return mapToResponse(updatedPost);
    }
    
    @Transactional(readOnly = true)
    public Long getPostCountByUser(String userId) {
        return postRepository.countByUserId(userId);
    }
    
    private PostResponse mapToResponse(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .content(post.getContent())
                .userId(post.getUserId())
                .username(post.getUsername())
                .createdAt(post.getCreatedAt())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .build();
    }
}
