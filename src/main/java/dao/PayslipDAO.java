package dao;

import dao.PayrollDAO.PayrollRow;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class PayslipDAO {

    private static final String PERIOD_STATUS = "OPEN";
    private static final String LATE_NAME = "Late";   // name of the late deduction type

    public static class SaveResult {
        public int saved;
        public int alreadySaved;
        public int noPay;
    }

    public SaveResult savePayslips(List<PayrollRow> rows, LocalDate from, LocalDate to,
                                   int processedBy) throws SQLException {
        SaveResult result = new SaveResult();
        try (Connection c = DBConnection.get()) {
            c.setAutoCommit(false);
            try {
                int periodId = findOrCreatePeriod(c, from, to);
                int lateTypeId = -1;   // looked up only when needed

                for (PayrollRow r : rows) {
                    if (r.present() + r.late() + r.onLeave() == 0 || r.gross() <= 0) {
                        result.noPay++;
                        continue;
                    }
                    if (payslipExists(c, periodId, r.employeeId())) {
                        result.alreadySaved++;
                        continue;
                    }

                    int payslipId = insertPayslip(c, periodId, r, processedBy);

                    if (r.deductions() > 0) {
                        if (lateTypeId < 0) lateTypeId = findOrCreateLateType(c);
                        insertDeduction(c, payslipId, lateTypeId, r.deductions());
                    }
                    result.saved++;
                }
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
        return result;
    }

    private int findOrCreatePeriod(Connection c, LocalDate from, LocalDate to) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT PERIOD_ID, STATUS FROM PAY_PERIODS WHERE PERIOD_START = ? AND PERIOD_END = ?")) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if ("CLOSED".equals(rs.getString(2))) {
                        throw new SQLException("This pay period is already CLOSED.");
                    }
                    return rs.getInt(1);
                }
            }
        }
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO PAY_PERIODS (PERIOD_START, PERIOD_END, PAY_DATE, STATUS) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            ps.setDate(3, Date.valueOf(to));          // pay date defaults to the period end
            ps.setString(4, PERIOD_STATUS);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private boolean payslipExists(Connection c, int periodId, int employeeId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM PAYSLIPS WHERE PERIOD_ID = ? AND EMPLOYEE_ID = ?")) {
            ps.setInt(1, periodId);
            ps.setInt(2, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    private int insertPayslip(Connection c, int periodId, PayrollRow r, int processedBy)
            throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO PAYSLIPS (PERIOD_ID, EMPLOYEE_ID, PROCESSED_BY, BASIC_PAY, OVERTIME_PAY, DATE_GENERATED) "
              + "VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, periodId);
            ps.setInt(2, r.employeeId());
            ps.setInt(3, processedBy);
            ps.setBigDecimal(4, money(r.gross()));
            ps.setBigDecimal(5, BigDecimal.ZERO);
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private int findOrCreateLateType(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT DEDUCTION_TYPE_ID FROM DEDUCTION_TYPES WHERE UPPER(DEDUCTION_NAME) = ?")) {
            ps.setString(1, LATE_NAME.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO DEDUCTION_TYPES (DEDUCTION_NAME, DESCRIPTION) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, LATE_NAME);
            ps.setString(2, "Deduction for late attendance");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private void insertDeduction(Connection c, int payslipId, int typeId, double amount)
            throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO PAYSLIP_DEDUCTIONS (PAYSLIP_ID, DEDUCTION_TYPE_ID, AMOUNT) VALUES (?, ?, ?)")) {
            ps.setInt(1, payslipId);
            ps.setInt(2, typeId);
            ps.setBigDecimal(3, money(amount));
            ps.executeUpdate();
        }
    }

    private static BigDecimal money(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP);
    }
}