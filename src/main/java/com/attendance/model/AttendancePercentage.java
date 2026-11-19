package com.attendance.model;

/** Attendance summary. subjectId/subjectName are null for the overall summary. */
public record AttendancePercentage(Integer studentId, String rollNo, String studentName,
                                   Integer subjectId, String subjectName,
                                   int totalClasses, int presentClasses, double percentage) {
}
