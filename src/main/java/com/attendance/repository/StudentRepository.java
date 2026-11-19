package com.attendance.repository;

import com.attendance.model.Student;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class StudentRepository {

    private final JdbcTemplate jdbc;

    public StudentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Student> mapper = (rs, i) -> new Student(
            rs.getInt("id"),
            rs.getString("roll_no"),
            rs.getString("name"),
            rs.getString("department"),
            rs.getString("email"));

    public List<Student> findAll() {
        return jdbc.query("SELECT * FROM students ORDER BY roll_no", mapper);
    }

    public Student findById(int id) {
        try {
            return jdbc.queryForObject("SELECT * FROM students WHERE id = ?", mapper, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<Student> search(String keyword) {
        String like = "%" + keyword.toLowerCase() + "%";
        return jdbc.query(
                "SELECT * FROM students WHERE LOWER(name) LIKE ? OR LOWER(roll_no) LIKE ? OR LOWER(department) LIKE ? ORDER BY roll_no",
                mapper, like, like, like);
    }

    public int insert(Student s) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO students (roll_no, name, department, email) VALUES (?, ?, ?, ?)",
                    new String[]{"id"});
            ps.setString(1, s.rollNo());
            ps.setString(2, s.name());
            ps.setString(3, s.department());
            ps.setString(4, s.email());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? -1 : key.intValue();
    }

    public int update(int id, Student s) {
        return jdbc.update(
                "UPDATE students SET roll_no = ?, name = ?, department = ?, email = ? WHERE id = ?",
                s.rollNo(), s.name(), s.department(), s.email(), id);
    }

    public int delete(int id) {
        return jdbc.update("DELETE FROM students WHERE id = ?", id);
    }
}

