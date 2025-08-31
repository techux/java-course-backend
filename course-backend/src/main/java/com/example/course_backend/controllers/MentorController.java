package com.example.course_backend.controllers;

import com.example.course_backend.repositories.MentorRepository;
import com.example.course_backend.repositories.UserRepository;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.example.course_backend.models.MentorProfile;


@RestController
@RequestMapping("/mentor")
public class MentorController {

    private final MentorRepository mentorRepo;
    private final UserRepository userRepo;

    public MentorController(MentorRepository mentorRepo, UserRepository userRepo) {
        this.mentorRepo = mentorRepo;
        this.userRepo = userRepo;
    }

    // all mentors
    @GetMapping("/all")
    public ResponseEntity<List<MentorProfile>> getAllMentors() {
        return ResponseEntity.ok(mentorRepo.findAll());
    }

    // add mentor
    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addMentor(@RequestBody MentorProfile mentorProfile) {
        mentorRepo.save(mentorProfile);
        return ResponseEntity.status(201).body(mentorProfile);
    }

    // Delete mentor (admin)
    @DeleteMapping("/delete/{mentorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteMentor(@PathVariable String mentorId) {
        mentorRepo.deleteById(mentorId);
        return ResponseEntity.noContent().build();
    }
}
