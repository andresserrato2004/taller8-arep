package com.example.stream.service;

import com.example.stream.dto.PostResponse;
import com.example.stream.dto.StreamResponse;
import com.example.stream.model.Stream;
import com.example.stream.repository.StreamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StreamService {
    
    @Autowired
    private StreamRepository streamRepository;
    
    @Transactional
    public StreamResponse createStream(String name, String description) {
        if (streamRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Ya existe un stream con ese nombre");
        }
        
        Stream stream = new Stream();
        stream.setName(name);
        stream.setDescription(description);
        
        Stream savedStream = streamRepository.save(stream);
        return mapToResponse(savedStream);
    }
    
    @Transactional(readOnly = true)
    public StreamResponse getStreamByName(String name) {
        Stream stream = streamRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Stream no encontrado"));
        return mapToResponse(stream);
    }
    
    @Transactional(readOnly = true)
    public List<StreamResponse> getAllStreams() {
        return streamRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    private StreamResponse mapToResponse(Stream stream) {
        List<PostResponse> posts = stream.getPosts().stream()
                .map(post -> PostResponse.builder()
                        .id(post.getId())
                        .content(post.getContent())
                        .userId(post.getUserId())
                        .username(post.getUsername())
                        .createdAt(post.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        
        return StreamResponse.builder()
                .id(stream.getId())
                .name(stream.getName())
                .description(stream.getDescription())
                .createdAt(stream.getCreatedAt())
                .posts(posts)
                .build();
    }
}
