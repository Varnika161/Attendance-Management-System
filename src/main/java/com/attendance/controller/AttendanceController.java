package com.attendance.controller;

import com.attendance.model.AttendancePercentage;
import com.attendance.model.AttendanceRecord;
import com.attendance.model.AttendanceRequest;
import com.attendance.service.AttendanceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService service;

    public AttendanceController(AttendanceService service) {
        this.service = service;
    }

    /** Mark or update attendance for one student, one subject, one date. */
    @PostMapping
    public Map<String, String> mark(@RequestBody AttendanceRequest request) {
        service.markAttendance(request);
        return Map.of("message", "Attendance saved");
    }

    @GetMapping("/student/{studentId}")
    public List<AttendanceRecord> byStudent(@PathVariable int studentId) {
        return service.byStudent(studentId);
    }

    @GetMapping("/subject/{subjectId}")
    public List<AttendanceRecord> bySubject(@PathVariable int subjectId) {
        return service.bySubject(subjectId);
    }

    @GetMapping("/date/{date}")
    public List<AttendanceRecord> byDate(@PathVariable String date) {
        return service.byDate(date);
    }

    /** Subject-wise percentage of one student. */
    @GetMapping("/percentage/{studentId}")
    public List<AttendancePercentage> subjectWise(@PathVariable int studentId) {
        return service.subjectWise(studentId);
    }

    /** Overall percentage of all students. */
    @GetMapping("/percentage")
    public List<AttendancePercentage> overall() {
        return service.overallAll();
    }

    /** Students below the threshold (default 75%). */
    @GetMapping("/low")
    public List<AttendancePercentage> low(@RequestParam(defaultValue = "75") double threshold) {
        return service.below(threshold);
    }
}
