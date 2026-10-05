/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.time.LocalDate;
import java.time.LocalTime;
/**
 *
 * @author alfre
 */
public class Attendance {
    private int attendanceId;
    private int employeeId;
    private LocalDate attendanceDate;
    private LocalTime timeIn;
    private LocalTime timeOut;
    private String status;

    public Attendance() { }

    public Attendance(int attendanceId, int employeeId, LocalDate attendanceDate, LocalTime timeIn, LocalTime timeOut,
            String status) {
        this.attendanceId = attendanceId;
        this.employeeId = employeeId;
        this.attendanceDate = attendanceDate;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
        this.status = status;
    }
    public int getAttendanceId() { return attendanceId; }
    public void setAttendanceId(int attendanceId) { this.attendanceId = attendanceId; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }

    public LocalTime getTimeIn() { return timeIn; }
    public void setTimeIn(LocalTime timeIn) { this.timeIn = timeIn; }

    public LocalTime getTimeOut() { return timeOut; }
    public void setTimeOut(LocalTime timeOut) { this.timeOut = timeOut; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

}
