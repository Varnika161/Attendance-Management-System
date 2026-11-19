package com.attendance.model;

/** One attendance entry. status: "P" = present, "A" = absent. date format: yyyy-MM-dd */
public record AttendanceRecord(Integer id, Integer studentId, String studentName,
                               Integer subjectId, String subjectName,
                               String date, String status) {
}
