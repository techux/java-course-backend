package com.example.course_backend.repositories;

import com.example.course_backend.models.MentorProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MentorRepository extends MongoRepository<MentorProfile, String> {
    Optional<MentorProfile> findByUserId(String userId);
    void deleteByUserId(String userId);
}
