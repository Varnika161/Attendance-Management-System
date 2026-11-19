package com.attendance.service;

import com.attendance.model.*;
import com.attendance.repository.AttendanceRepository;
import com.attendance.repository.StudentRepository;
import com.attendance.repository.SubjectRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class AttendanceService {

    private final StudentRepository students;
    private final SubjectRepository subjects;
    private final AttendanceRepository attendance;

    public AttendanceService(StudentRepository students, SubjectRepository subjects, AttendanceRepository attendance) {
        this.students = students;
        this.subjects = subjects;
        this.attendance = attendance;
    }

    // ---------- Students ----------
    public List<Student> allStudents() {
        return students.findAll();
    }

    public Student getStudent(int id) {
        Student s = students.findById(id);
        if (s == null) throw new IllegalArgumentException("Student not found with id " + id);
        return s;
    }

    public List<Student> searchStudents(String keyword) {
        return students.search(keyword);
    }

    public Student addStudent(Student s) {
        validate(s);
        int id = students.insert(s);
        return getStudent(id);
    }

    public Student updateStudent(int id, Student s) {
        validate(s);
        if (students.update(id, s) == 0) throw new IllegalArgumentException("Student not found with id " + id);
        return getStudent(id);
    }

    public void deleteStudent(int id) {
        if (students.delete(id) == 0) throw new IllegalArgumentException("Student not found with id " + id);
    }

    private void validate(Student s) {
        if (isBlank(s.rollNo()) || isBlank(s.name()) || isBlank(s.department())) {
            throw new IllegalArgumentException("rollNo, name and department are required");
        }
    }

    // ---------- Subjects ----------
    public List<Subject> allSubjects() {
        return subjects.findAll();
    }

    public void addSubject(String name) {
        if (isBlank(name)) throw new IllegalArgumentException("Subject name is required");
        subjects.insert(name.trim());
    }

    // ---------- Attendance ----------
    public void markAttendance(AttendanceRequest r) {
        if (r.studentId() == null || r.subjectId() == null || isBlank(r.date()) || isBlank(r.status())) {
            throw new IllegalArgumentException("studentId, subjectId, date and status are required");
        }
        String status = r.status().trim().toUpperCase();
        if (!status.equals("P") && !status.equals("A")) {
            throw new IllegalArgumentException("status must be P (present) or A (absent)");
        }
        getStudent(r.studentId());
        attendance.mark(r.studentId(), r.subjectId(), parseDate(r.date()), status);
    }

    public List<AttendanceRecord> byStudent(int studentId) {
        getStudent(studentId);
        return attendance.findByStudent(studentId);
    }

    public List<AttendanceRecord> bySubject(int subjectId) {
        return attendance.findBySubject(subjectId);
    }

    public List<AttendanceRecord> byDate(String date) {
        return attendance.findByDate(parseDate(date));
    }

    public List<AttendancePercentage> subjectWise(int studentId) {
        getStudent(studentId);
        return attendance.subjectWisePercentage(studentId);
    }

    public List<AttendancePercentage> overallAll() {
        return attendance.overallPercentageAll();
    }

    public List<AttendancePercentage> below(double threshold) {
        return attendance.belowThreshold(threshold);
    }

    private LocalDate parseDate(String d) {
        try {
            return LocalDate.parse(d.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Date must be in yyyy-MM-dd format, e.g. 2026-11-14");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
