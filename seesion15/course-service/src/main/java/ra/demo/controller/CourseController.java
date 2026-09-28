package ra.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    // Danh sách khóa học giả (Java List tĩnh)
    private final List<Map<String, Object>> courses = new ArrayList<>(List.of(
            Map.of("id", 1, "title", "Lập trình Microservices với Spring Boot"),
            Map.of("id", 2, "title", "Chinh phục Java Core")
    ));

    // GET /api/courses: Cả STUDENT hoặc INSTRUCTOR đều xem được
    @GetMapping
    @PreAuthorize("hasAuthority('STUDENT') or hasAuthority('INSTRUCTOR')")
    public ResponseEntity<?> getAllCourses() {
        return ResponseEntity.ok(courses);
    }

    // POST /api/courses: Chỉ INSTRUCTOR mới được tạo khóa học
    @PostMapping
    @PreAuthorize("hasAuthority('INSTRUCTOR')")
    public ResponseEntity<String> createCourse(@RequestBody Map<String, Object> newCourse) {
        courses.add(newCourse);
        return ResponseEntity.ok("Tạo khóa học thành công!");
    }
}