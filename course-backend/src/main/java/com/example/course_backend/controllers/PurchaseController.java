package com.example.course_backend.controllers;

import com.example.course_backend.models.Course;
import com.example.course_backend.models.Purchase;
import com.example.course_backend.models.User;
import com.example.course_backend.repositories.CourseRepository;
import com.example.course_backend.repositories.PurchaseRepository;
import com.example.course_backend.repositories.UserRepository;
import com.example.course_backend.security.JwtUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/purchase")
public class PurchaseController {

    private final PurchaseRepository purchaseRepo;
    private final CourseRepository courseRepo;
    private final UserRepository userRepo;
    private final JwtUtils jwtUtils;

    public PurchaseController(PurchaseRepository purchaseRepo,
                              CourseRepository courseRepo,
                              UserRepository userRepo,
                              JwtUtils jwtUtils) {
        this.purchaseRepo = purchaseRepo;
        this.courseRepo = courseRepo;
        this.userRepo = userRepo;
        this.jwtUtils = jwtUtils;
    }

    // Purchase course
    @PostMapping("/{courseId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> purchase(@PathVariable String courseId, @RequestBody Purchase body, HttpServletRequest req) {
        String token = extractToken(req);
        String buyerId = jwtUtils.getUserIdFromToken(token);

        Optional<Course> courseOpt = courseRepo.findById(courseId);
        if (courseOpt.isEmpty()) return ResponseEntity.notFound().build();
        Course course = courseOpt.get();

        // Prevent double purchase (simple)
        if (purchaseRepo.existsByBuyerUserIdAndCourseId(buyerId, courseId)) {
            return ResponseEntity.badRequest().body("Already purchased");
        }

        Purchase p = new Purchase();
        p.setCourseId(courseId);
        p.setBuyerUserId(buyerId);
        p.setPricePaid(course.getPrice());
        p.setPaymentReference(body.getPaymentReference());
        purchaseRepo.save(p);
        return ResponseEntity.status(201).body(p);
    }

    // My courses
    @GetMapping("/mycourse")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> myCourses(HttpServletRequest req) {
        String token = extractToken(req);
        String buyerId = jwtUtils.getUserIdFromToken(token);
        List<Purchase> list = purchaseRepo.findByBuyerUserId(buyerId);
        return ResponseEntity.ok(list);
    }

    private String extractToken(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) throw new IllegalArgumentException("Missing token");
        return header.substring(7);
    }
}
