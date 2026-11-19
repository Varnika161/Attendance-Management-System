package com.attendance.repository;

import com.attendance.model.AttendancePercentage;
import com.attendance.model.AttendanceRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Repository
public class AttendanceRepository {

    private final JdbcTemplate jdbc;

    public AttendanceRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String BASE_SELECT =
            "SELECT a.id, a.student_id, s.name AS student_name, a.subject_id, sub.name AS subject_name, " +
            "a.att_date, a.status " +
            "FROM attendance a " +
            "JOIN students s ON s.id = a.student_id " +
            "JOIN subjects sub ON sub.id = a.subject_id ";

    private final RowMapper<AttendanceRecord> recordMapper = (rs, i) -> new AttendanceRecord(
            rs.getInt("id"),
            rs.getInt("student_id"),
            rs.getString("student_name"),
            rs.getInt("subject_id"),
            rs.getString("subject_name"),
            rs.getDate("att_date").toString(),
            rs.getString("status"));

    /** Mark attendance. If a record already exists for (student, subject, date) it is updated. */
    public void mark(int studentId, int subjectId, LocalDate date, String status) {
        int updated = jdbc.update(
                "UPDATE attendance SET status = ? WHERE student_id = ? AND subject_id = ? AND att_date = ?",
                status, studentId, subjectId, Date.valueOf(date));
        if (updated == 0) {
            jdbc.update(
                    "INSERT INTO attendance (student_id, subject_id, att_date, status) VALUES (?, ?, ?, ?)",
                    studentId, subjectId, Date.valueOf(date), status);
        }
    }

    public List<AttendanceRecord> findByStudent(int studentId) {
        return jdbc.query(BASE_SELECT + "WHERE a.student_id = ? ORDER BY a.att_date DESC, sub.name",
                recordMapper, studentId);
    }

    public List<AttendanceRecord> findBySubject(int subjectId) {
        return jdbc.query(BASE_SELECT + "WHERE a.subject_id = ? ORDER BY a.att_date DESC, s.name",
                recordMapper, subjectId);
    }

    public List<AttendanceRecord> findByDate(LocalDate date) {
        return jdbc.query(BASE_SELECT + "WHERE a.att_date = ? ORDER BY sub.name, s.name",
                recordMapper, Date.valueOf(date));
    }

    /** Subject-wise attendance percentage for one student. */
    public List<AttendancePercentage> subjectWisePercentage(int studentId) {
        String sql =
                "SELECT s.id AS student_id, s.roll_no, s.name AS student_name, sub.id AS subject_id, sub.name AS subject_name, " +
                "COUNT(a.id) AS total, SUM(CASE WHEN a.status = 'P' THEN 1 ELSE 0 END) AS present " +
                "FROM attendance a " +
                "JOIN students s ON s.id = a.student_id " +
                "JOIN subjects sub ON sub.id = a.subject_id " +
                "WHERE s.id = ? " +
                "GROUP BY s.id, s.roll_no, s.name, sub.id, sub.name " +
                "ORDER BY sub.name";
        return jdbc.query(sql, (rs, i) -> build(rs.getInt("student_id"), rs.getString("roll_no"),
                rs.getString("student_name"), rs.getInt("subject_id"), rs.getString("subject_name"),
                rs.getInt("total"), rs.getInt("present")), studentId);
    }

    /** Overall attendance percentage of every student (all subjects together). */
    public List<AttendancePercentage> overallPercentageAll() {
        String sql =
                "SELECT s.id AS student_id, s.roll_no, s.name AS student_name, " +
                "COUNT(a.id) AS total, SUM(CASE WHEN a.status = 'P' THEN 1 ELSE 0 END) AS present " +
                "FROM students s " +
                "JOIN attendance a ON a.student_id = s.id " +
                "GROUP BY s.id, s.roll_no, s.name " +
                "ORDER BY s.roll_no";
        return jdbc.query(sql, (rs, i) -> build(rs.getInt("student_id"), rs.getString("roll_no"),
                rs.getString("student_name"), null, null,
                rs.getInt("total"), rs.getInt("present")));
    }

    /** Students whose overall attendance is below the given percentage (default use: 75). */
    public List<AttendancePercentage> belowThreshold(double threshold) {
        String sql =
                "SELECT s.id AS student_id, s.roll_no, s.name AS student_name, " +
                "COUNT(a.id) AS total, SUM(CASE WHEN a.status = 'P' THEN 1 ELSE 0 END) AS present " +
                "FROM students s " +
                "JOIN attendance a ON a.student_id = s.id " +
                "GROUP BY s.id, s.roll_no, s.name " +
                "HAVING (SUM(CASE WHEN a.status = 'P' THEN 1 ELSE 0 END) * 100.0 / COUNT(a.id)) < ? " +
                "ORDER BY s.roll_no";
        return jdbc.query(sql, (rs, i) -> build(rs.getInt("student_id"), rs.getString("roll_no"),
                rs.getString("student_name"), null, null,
                rs.getInt("total"), rs.getInt("present")), threshold);
    }

    private AttendancePercentage build(int studentId, String rollNo, String name,
                                       Integer subjectId, String subjectName, int total, int present) {
        double pct = total == 0 ? 0 : Math.round(present * 10000.0 / total) / 100.0;
        return new AttendancePercentage(studentId, rollNo, name, subjectId, subjectName, total, present, pct);
    }
}
