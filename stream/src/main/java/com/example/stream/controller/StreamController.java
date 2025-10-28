package com.example.stream.controller;

import com.example.stream.dto.StreamResponse;
import com.example.stream.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/streams")
@CrossOrigin(origins = "*")
public class StreamController {
    
    @Autowired
    private StreamService streamService;
    
    @PostMapping
    public ResponseEntity<?> createStream(@RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String description = request.get("description");
            
            StreamResponse stream = streamService.createStream(name, description);
            return ResponseEntity.status(HttpStatus.CREATED).body(stream);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al crear el stream: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping
    public ResponseEntity<List<StreamResponse>> getAllStreams() {
        List<StreamResponse> streams = streamService.getAllStreams();
        return ResponseEntity.ok(streams);
    }
    
    @GetMapping("/public")
    public ResponseEntity<List<StreamResponse>> getAllStreamsPublic() {
        List<StreamResponse> streams = streamService.getAllStreams();
        return ResponseEntity.ok(streams);
    }
    
    @GetMapping("/{name}")
    public ResponseEntity<?> getStreamByName(@PathVariable String name) {
        try {
            StreamResponse stream = streamService.getStreamByName(name);
            return ResponseEntity.ok(stream);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}
