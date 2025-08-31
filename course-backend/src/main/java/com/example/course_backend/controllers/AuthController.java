package com.example.course_backend.controllers;

import com.example.course_backend.dto.AuthRequests.*;
import com.example.course_backend.dto.AuthResponse;
import com.example.course_backend.models.MentorProfile;
import com.example.course_backend.models.Role;
import com.example.course_backend.models.User;
import com.example.course_backend.repositories.MentorRepository;
import com.example.course_backend.repositories.UserRepository;
import com.example.course_backend.security.JwtUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepo;
    private final MentorRepository mentorRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final long expiresIn;

    public AuthController(UserRepository userRepo,
                          MentorRepository mentorRepo,
                          PasswordEncoder passwordEncoder,
                          JwtUtils jwtUtils) {
        this.userRepo = userRepo;
        this.mentorRepo = mentorRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.expiresIn = Long.parseLong(System.getProperty("jwt.expiration-ms", "3600000"));
    }

    @PostMapping("/register/customer")
    public ResponseEntity<?> registerCustomer(@RequestBody RegisterCustomerRequest req) {
        if (userRepo.existsByEmail(req.getEmail())) {
            return ResponseEntity.badRequest().body("Email already in use");
        }
        User u = new User();
        u.setName(req.getName());
        u.setEmail(req.getEmail());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setRole(Role.CUSTOMER);
        u.setCreatedAt(Instant.now());
        userRepo.save(u);
        return ResponseEntity.status(201).body(u);
    }

    @PostMapping("/register/mentor")
    public ResponseEntity<?> registerMentor(@RequestBody RegisterMentorRequest req) {
        if (userRepo.existsByEmail(req.getEmail())) {
            return ResponseEntity.badRequest().body("Email already in use");
        }
        User u = new User();
        u.setName(req.getName());
        u.setEmail(req.getEmail());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setRole(Role.MENTOR);
        u.setCreatedAt(Instant.now());
        userRepo.save(u);

        MentorProfile m = new MentorProfile();
        m.setUserId(u.getId());
        m.setBio(req.getBio());
        m.setExpertise(req.getExpertise());
        mentorRepo.save(m);

        return ResponseEntity.status(201).body(u);
    }

    @PostMapping("/login/{role}")
    public ResponseEntity<?> login(@PathVariable String role, @RequestBody LoginRequest req) {
        Optional<User> opt = userRepo.findByEmail(req.getEmail());
        if (opt.isEmpty()) return ResponseEntity.status(401).body("Invalid credentials");
        User u = opt.get();
        if (!passwordEncoder.matches(req.getPassword(), u.getPassword())) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
        if (!u.getRole().name().equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Wrong role for this login endpoint");
        }
        String token = jwtUtils.generateToken(u.getId(), u.getEmail(), u.getRole().name());
        AuthResponse resp = new AuthResponse();
        resp.setAccessToken(token);
        resp.setExpiresIn(expiresIn);
        resp.setUserId(u.getId());
        resp.setName(u.getName());
        resp.setEmail(u.getEmail());
        resp.setRole(u.getRole());
        return ResponseEntity.ok(resp);
    }
}
