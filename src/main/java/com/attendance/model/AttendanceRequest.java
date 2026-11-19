package com.attendance.model;

/** Request body used when marking attendance. */
public record AttendanceRequest(Integer studentId, Integer subjectId, String date, String status) {
}
