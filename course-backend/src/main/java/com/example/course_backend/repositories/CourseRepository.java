package com.example.course_backend.repositories;

import com.example.course_backend.models.Course;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CourseRepository extends MongoRepository<Course, String> {
    List<Course> findByTitleContainingIgnoreCase(String q);
    List<Course> findByMentorUserId(String mentorUserId);
    List<Course> findByCategoryId(String categoryId);
}
