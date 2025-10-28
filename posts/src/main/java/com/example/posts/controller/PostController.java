package com.example.posts.controller;

import com.example.posts.dto.PostRequest;
import com.example.posts.dto.PostResponse;
import com.example.posts.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "*")
public class PostController {
    
    @Autowired
    private PostService postService;
    
    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody PostRequest request, Authentication authentication) {
        try {
            // Extraer información del usuario del JWT
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String userId = jwt.getSubject();
            
            // Intentar extraer el username de diferentes claims
            String username = jwt.getClaim("username");
            if (username == null) {
                username = jwt.getClaim("cognito:username");
            }
            if (username == null) {
                username = jwt.getClaim("preferred_username");
            }
            if (username == null) {
                username = jwt.getClaim("email");
            }
            
            PostResponse post = postService.createPost(request, userId, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(post);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al crear el post: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        List<PostResponse> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts);
    }
    
    @GetMapping("/public")
    public ResponseEntity<List<PostResponse>> getAllPostsPublic() {
        List<PostResponse> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostResponse>> getPostsByUser(@PathVariable String userId) {
        List<PostResponse> posts = postService.getPostsByUser(userId);
        return ResponseEntity.ok(posts);
    }
    
    @GetMapping("/my-posts")
    public ResponseEntity<List<PostResponse>> getMyPosts(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String userId = jwt.getSubject();
        
        List<PostResponse> posts = postService.getPostsByUser(userId);
        return ResponseEntity.ok(posts);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getPostById(@PathVariable Long id) {
        try {
            PostResponse post = postService.getPostById(id);
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(@PathVariable Long id, @RequestBody PostRequest request, Authentication authentication) {
        try {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String userId = jwt.getSubject();
            
            PostResponse post = postService.updatePost(id, request, userId);
            return ResponseEntity.ok(post);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id, Authentication authentication) {
        try {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String userId = jwt.getSubject();
            
            postService.deletePost(id, userId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Post eliminado exitosamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
    }
    
    @PostMapping("/{id}/like")
    public ResponseEntity<?> likePost(@PathVariable Long id) {
        try {
            PostResponse post = postService.likePost(id);
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @GetMapping("/count/user/{userId}")
    public ResponseEntity<?> getPostCountByUser(@PathVariable String userId) {
        Long count = postService.getPostCountByUser(userId);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
}
