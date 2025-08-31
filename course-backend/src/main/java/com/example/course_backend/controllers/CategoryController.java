package com.example.course_backend.controllers;

import com.example.course_backend.models.Category;
import com.example.course_backend.repositories.CategoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    private final CategoryRepository repo;

    public CategoryController(CategoryRepository repo) {
        this.repo = repo;
    }

    // Add
    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> add(@RequestBody Category c) {
        if (repo.existsByName(c.getName())) return ResponseEntity.badRequest().body("Category exists");
        c.setCreatedAt(Instant.now());
        repo.save(c);
        return ResponseEntity.status(201).body(c);
    }

    // Update
    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@RequestBody Category c) {
        return repo.findById(c.getId()).map(existing -> {
            existing.setName(c.getName());
            existing.setDescription(c.getDescription());
            existing.setUpdatedAt(Instant.now());
            repo.save(existing);
            return ResponseEntity.ok(existing);
        }).orElse(ResponseEntity.notFound().build());
    }

    // Delete
    @DeleteMapping("/delete/{catId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable String catId) {
        repo.deleteById(catId);
        return ResponseEntity.noContent().build();
    }

    // All
    @GetMapping("/all")
    public ResponseEntity<List<Category>> all() {
        return ResponseEntity.ok(repo.findAll());
    }

    @GetMapping("/{catId}")
    public ResponseEntity<Category> get(@PathVariable String catId) {
        return repo.findById(catId).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
