package dao;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PayrollDAO {

    // ---- pay rules (change as needed) ----
    public static final double WORKING_DAYS_PER_MONTH = 26;
    public static final double LATE_DEDUCTION = 100.0;   // per late

    public record PayrollRow(String employeeNo, String name,
                             double monthlySalary, double dailyRate,
                             int present, int late, int onLeave, int absent,
                             double gross, double deductions, double net,
                             int employeeId) {}

    public List<PayrollRow> compute(LocalDate from, LocalDate to) throws SQLException {
        String sql =
            "SELECT e.employee_no, e.first_name, e.last_name, jp.monthly_salary, "
          + "SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END), "
          + "SUM(CASE WHEN a.status = 'LATE' THEN 1 ELSE 0 END), "
          + "SUM(CASE WHEN a.status = 'ON_LEAVE' THEN 1 ELSE 0 END), "
          + "SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END), "
          + "e.employee_id "
          + "FROM EMPLOYEES e "
          + "LEFT JOIN JOB_POSITIONS jp ON jp.position_id = e.position_id "
          + "LEFT JOIN ATTENDANCE a "
          + "ON a.employee_id = e.employee_id AND a.attendance_date BETWEEN ? AND ? "
          + "WHERE e.status = 'ACTIVE' "
          + "GROUP BY e.employee_id, e.employee_no, e.first_name, e.last_name, jp.monthly_salary "
          + "ORDER BY e.employee_no";

        List<PayrollRow> rows = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String no = rs.getString(1);
                    String name = rs.getString(2) + " " + rs.getString(3);
                    double monthly = parseMoney(rs.getString(4));
                    int present = rs.getInt(5);
                    int late = rs.getInt(6);
                    int leave = rs.getInt(7);
                    int absent = rs.getInt(8);
                    int empId = rs.getInt(9);

                    double daily = monthly / WORKING_DAYS_PER_MONTH;
                    double gross = daily * (present + late + leave);
                    double deductions = late * LATE_DEDUCTION;
                    double net = Math.max(0, gross - deductions);

                    rows.add(new PayrollRow(no, name, monthly, daily,
                            present, late, leave, absent, gross, deductions, net, empId));
                }
            }
        }
        return rows;
    }

    /** Reads a salary even if it is stored as text such as "25,000.00". */
    private static double parseMoney(String s) {
        if (s == null) return 0;
        try {
            return Double.parseDouble(s.replace(",", "").trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}