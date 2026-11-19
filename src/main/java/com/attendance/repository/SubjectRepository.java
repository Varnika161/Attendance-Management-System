package com.attendance.repository;

import com.attendance.model.Subject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SubjectRepository {

    private final JdbcTemplate jdbc;

    public SubjectRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Subject> findAll() {
        return jdbc.query("SELECT id, name FROM subjects ORDER BY name",
                (rs, i) -> new Subject(rs.getInt("id"), rs.getString("name")));
    }

    public int insert(String name) {
        return jdbc.update("INSERT INTO subjects (name) VALUES (?)", name);
    }
}
