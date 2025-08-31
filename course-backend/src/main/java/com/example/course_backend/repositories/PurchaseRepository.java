package com.example.course_backend.repositories;

import com.example.course_backend.models.Purchase;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PurchaseRepository extends MongoRepository<Purchase, String> {
    List<Purchase> findByBuyerUserId(String buyerUserId);
    boolean existsByBuyerUserIdAndCourseId(String buyerUserId, String courseId);
}
