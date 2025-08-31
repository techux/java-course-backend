package com.example.course_backend.controllers;

import com.example.course_backend.models.Course;
import com.example.course_backend.models.Role;
import com.example.course_backend.models.User;
import com.example.course_backend.repositories.CourseRepository;
import com.example.course_backend.repositories.UserRepository;
import com.example.course_backend.security.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/course")
public class CourseController {

    private final CourseRepository courseRepo;
    private final UserRepository userRepo;
    private final JwtUtils jwtUtils;

    public CourseController(CourseRepository courseRepo, UserRepository userRepo, JwtUtils jwtUtils) {
        this.courseRepo = courseRepo;
        this.userRepo = userRepo;
        this.jwtUtils = jwtUtils;
    }

    // Add course (mentor or admin)
    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('MENTOR','ADMIN')")
    public ResponseEntity<?> add(@RequestBody Course c, HttpServletRequest req) {
        // get userId from token
        String token = extractToken(req);
        String uid = jwtUtils.getUserIdFromToken(token);
        // if mentor, set mentorUserId to current user; if admin, mentorUserId must be provided
        Optional<User> uopt = userRepo.findById(uid);
        if (uopt.isEmpty()) return ResponseEntity.status(401).body("Invalid user");
        User u = uopt.get();
        if (u.getRole() == Role.MENTOR) {
            c.setMentorUserId(uid);
        } else if (u.getRole() == Role.ADMIN && (c.getMentorUserId() == null || c.getMentorUserId().isBlank())) {
            return ResponseEntity.badRequest().body("Admin must provide mentorUserId when creating course");
        }
        c.setCreatedAt(Instant.now());
        courseRepo.save(c);
        return ResponseEntity.status(201).body(c);
    }

    // View by id
    @GetMapping("/view/{courseId}")
    public ResponseEntity<?> view(@PathVariable String courseId) {
        return courseRepo.findById(courseId).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // Update
    @PostMapping("/update/{courseId}")
    @PreAuthorize("hasAnyRole('MENTOR','ADMIN')")
    public ResponseEntity<?> update(@PathVariable String courseId, @RequestBody Course update, HttpServletRequest req) {
        String token = extractToken(req);
        String uid = jwtUtils.getUserIdFromToken(token);
        Optional<Course> opt = courseRepo.findById(courseId);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Course existing = opt.get();
        // allow if admin or owner mentor
        Optional<User> uopt = userRepo.findById(uid);
        if (uopt.isEmpty()) return ResponseEntity.status(401).body("Invalid user");
        User user = uopt.get();
        if (user.getRole() == Role.MENTOR && !uid.equals(existing.getMentorUserId())) {
            return ResponseEntity.status(403).body("Not your course");
        }
        // apply updates
        existing.setTitle(update.getTitle());
        existing.setDescription(update.getDescription());
        existing.setPrice(update.getPrice());
        existing.setThumbnailUrl(update.getThumbnailUrl());
        existing.setPublished(update.isPublished());
        existing.setUpdatedAt(Instant.now());
        courseRepo.save(existing);
        return ResponseEntity.ok(existing);
    }

    // Delete
    @DeleteMapping("/delete/{courseId}")
    @PreAuthorize("hasAnyRole('MENTOR','ADMIN')")
    public ResponseEntity<?> delete(@PathVariable String courseId, HttpServletRequest req) {
        String token = extractToken(req);
        String uid = jwtUtils.getUserIdFromToken(token);
        Optional<Course> opt = courseRepo.findById(courseId);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Course existing = opt.get();
        Optional<User> uopt = userRepo.findById(uid);
        if (uopt.isEmpty()) return ResponseEntity.status(401).body("Invalid user");
        User user = uopt.get();
        if (user.getRole() == Role.MENTOR && !uid.equals(existing.getMentorUserId())) {
            return ResponseEntity.status(403).body("Not your course");
        }
        courseRepo.deleteById(courseId);
        return ResponseEntity.noContent().build();
    }

    // Search
    @GetMapping("/search")
    public ResponseEntity<List<Course>> search(@RequestParam(name = "q", required = false) String q) {
        if (q == null || q.isBlank()) {
            return ResponseEntity.ok(courseRepo.findAll());
        } else {
            return ResponseEntity.ok(courseRepo.findByTitleContainingIgnoreCase(q));
        }
    }

    private String extractToken(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) throw new IllegalArgumentException("Missing token");
        return header.substring(7);
    }
}
