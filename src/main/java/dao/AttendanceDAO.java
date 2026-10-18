package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import model.Attendance;

public class AttendanceDAO {

    public List<Attendance> findForDate(LocalDate date) throws SQLException {
        String sql = "SELECT e.employee_id, e.employee_no, e.first_name, e.last_name, "
                   + "a.attendance_id, a.time_in, a.time_out, a.status "
                   + "FROM EMPLOYEES e "
                   + "LEFT JOIN ATTENDANCE a "
                   + "  ON a.employee_id = e.employee_id AND a.attendance_date = ? "
                   + "WHERE e.status = 'ACTIVE' "
                   + "ORDER BY e.last_name, e.first_name";

        List<Attendance> list = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Attendance a = new Attendance();
                    a.setEmployeeId(rs.getInt("employee_id"));
                    a.setEmployeeNo(rs.getString("employee_no"));
                    a.setEmployeeName(rs.getString("last_name") + ", " + rs.getString("first_name"));
                    a.setAttendanceDate(date);
                    a.setAttendanceId(rs.getInt("attendance_id"));   

                    Time in = rs.getTime("time_in");
                    Time out = rs.getTime("time_out");
                    a.setTimeIn(in == null ? null : in.toLocalTime());
                    a.setTimeOut(out == null ? null : out.toLocalTime());

                    a.setStatus(rs.getString("status"));              
                    list.add(a);
                }
            }
        }
        return list;
    }

    public void saveAll(LocalDate date, List<Attendance> rows) throws SQLException {
        String updateSql = "UPDATE ATTENDANCE SET time_in = ?, time_out = ?, status = ? "
                         + "WHERE employee_id = ? AND attendance_date = ?";
        String insertSql = "INSERT INTO ATTENDANCE (employee_id, attendance_date, time_in, time_out, status) "
                         + "VALUES (?, ?, ?, ?, ?)";

        try (Connection c = DBConnection.get()) {
            c.setAutoCommit(false);
            try {
                for (Attendance a : rows) {
                    int updated;
                    try (PreparedStatement ps = c.prepareStatement(updateSql)) {
                        setTime(ps, 1, a.getTimeIn());
                        setTime(ps, 2, a.getTimeOut());
                        ps.setString(3, a.getStatus());
                        ps.setInt(4, a.getEmployeeId());
                        ps.setDate(5, Date.valueOf(date));
                        updated = ps.executeUpdate();
                    }

                    if (updated == 0) {
                        try (PreparedStatement ps = c.prepareStatement(insertSql)) {
                            ps.setInt(1, a.getEmployeeId());
                            ps.setDate(2, Date.valueOf(date));
                            setTime(ps, 3, a.getTimeIn());
                            setTime(ps, 4, a.getTimeOut());
                            ps.setString(5, a.getStatus());
                            ps.executeUpdate();
                        }
                    }
                }
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    private void setTime(PreparedStatement ps, int index, LocalTime time) throws SQLException {
        if (time == null) {
            ps.setNull(index, Types.TIME);
        } else {
            ps.setTime(index, Time.valueOf(time));
        }
    }
}