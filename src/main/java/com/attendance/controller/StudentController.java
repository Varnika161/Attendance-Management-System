package com.attendance.controller;

import com.attendance.model.Student;
import com.attendance.model.Subject;
import com.attendance.service.AttendanceService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class StudentController {

    private final AttendanceService service;

    public StudentController(AttendanceService service) {
        this.service = service;
    }

    @GetMapping("/students")
    public List<Student> all() {
        return service.allStudents();
    }

    @GetMapping("/students/search")
    public List<Student> search(@RequestParam String keyword) {
        return service.searchStudents(keyword);
    }

    @GetMapping("/students/{id}")
    public Student one(@PathVariable int id) {
        return service.getStudent(id);
    }

    @PostMapping("/students")
    @ResponseStatus(HttpStatus.CREATED)
    public Student add(@RequestBody Student s) {
        return service.addStudent(s);
    }

    @PutMapping("/students/{id}")
    public Student update(@PathVariable int id, @RequestBody Student s) {
        return service.updateStudent(id, s);
    }

    @DeleteMapping("/students/{id}")
    public Map<String, String> delete(@PathVariable int id) {
        service.deleteStudent(id);
        return Map.of("message", "Student deleted");
    }

    @GetMapping("/subjects")
    public List<Subject> subjects() {
        return service.allSubjects();
    }

    @PostMapping("/subjects")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> addSubject(@RequestBody Map<String, String> body) {
        service.addSubject(body.get("name"));
        return Map.of("message", "Subject added");
    }
}
