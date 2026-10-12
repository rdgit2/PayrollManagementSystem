package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;

public class DashboardDAO {

    public record Stats(int active, int present, int late, int absent,
                        String period, BigDecimal gross, BigDecimal deductions,
                        BigDecimal net, int payslips) {}

    public Stats load() throws SQLException {
        try (Connection c = DBConnection.get()) {
            int active = count(c, "SELECT COUNT(*) FROM EMPLOYEES WHERE status = 'ACTIVE'");
            int present = count(c, "SELECT COUNT(*) FROM ATTENDANCE WHERE attendance_date = CURRENT_DATE AND status = 'PRESENT'");
            int late = count(c, "SELECT COUNT(*) FROM ATTENDANCE WHERE attendance_date = CURRENT_DATE AND status = 'LATE'");
            int absent = count(c, "SELECT COUNT(*) FROM ATTENDANCE WHERE attendance_date = CURRENT_DATE AND status = 'ABSENT'");

            String period = null;
            int periodId = -1;
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d, yyyy");
            String openSql = "SELECT period_id, period_start, period_end FROM PAY_PERIODS "
                           + "WHERE status = 'OPEN' ORDER BY period_start DESC FETCH FIRST ROW ONLY";
            try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(openSql)) {
                if (rs.next()) {
                    periodId = rs.getInt(1);
                    period = rs.getDate(2).toLocalDate().format(fmt) + " - "
                           + rs.getDate(3).toLocalDate().format(fmt);
                }
            }

            BigDecimal gross = BigDecimal.ZERO, ded = BigDecimal.ZERO, net = BigDecimal.ZERO;
            int payslips = 0;
            if (periodId > 0) {
                String sumSql = "SELECT COALESCE(SUM(gross_pay), 0), COALESCE(SUM(total_deductions), 0), "
                              + "COALESCE(SUM(net_pay), 0), COUNT(*) FROM PAYSLIP_SUMMARY WHERE period_id = ?";
                try (PreparedStatement ps = c.prepareStatement(sumSql)) {
                    ps.setInt(1, periodId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            gross = rs.getBigDecimal(1);
                            ded = rs.getBigDecimal(2);
                            net = rs.getBigDecimal(3);
                            payslips = rs.getInt(4);
                        }
                    }
                }
            }
            return new Stats(active, present, late, absent, period, gross, ded, net, payslips);
        }
    }

    private int count(Connection c, String sql) throws SQLException {
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}