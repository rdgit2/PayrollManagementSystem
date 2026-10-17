package dao;

import dao.PayrollDAO.PayrollRow;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PayslipDAO {

    private static final String PERIOD_STATUS = "OPEN";
    private static final String LATE_NAME = "Late/Absent";

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
                int lateTypeId = -1;  

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

    public record PeriodItem(int periodId, LocalDate start, LocalDate end, String status) {
        @Override
        public String toString() {
            DateTimeFormatter f = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);
            return f.format(start) + " - " + f.format(end) + " (" + status + ")";
        }
    }

    /** One row of the employee combo box (a saved payslip). */
    public record PayslipItem(int payslipId, int employeeId, String employeeNo, String name) {
        @Override
        public String toString() {
            return name + " (" + employeeNo + ")";
        }
    }

  
    public record PayslipDetail(String title, String subtitle, double basic, double overtime,
                                String[] deductionNames, double[] deductionAmounts) {}

 
    public List<PeriodItem> listPeriods() throws SQLException {
        String sql = "SELECT period_id, period_start, period_end, status "
                   + "FROM PAY_PERIODS ORDER BY period_start DESC";
        List<PeriodItem> list = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new PeriodItem(rs.getInt(1), rs.getDate(2).toLocalDate(),
                        rs.getDate(3).toLocalDate(), rs.getString(4)));
            }
        }
        return list;
    }


    public List<PayslipItem> listPayslips(int periodId, Integer onlyEmployeeId) throws SQLException {
        String sql = "SELECT p.payslip_id, e.employee_id, e.employee_no, e.first_name, e.last_name "
                   + "FROM PAYSLIPS p INNER JOIN EMPLOYEES e ON p.employee_id = e.employee_id "
                   + "WHERE p.period_id = ? "
                   + (onlyEmployeeId != null ? "AND e.employee_id = ? " : "")
                   + "ORDER BY e.last_name, e.first_name";
        List<PayslipItem> list = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, periodId);
            if (onlyEmployeeId != null) ps.setInt(2, onlyEmployeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new PayslipItem(rs.getInt(1), rs.getInt(2), rs.getString(3),
                            rs.getString(5) + ", " + rs.getString(4)));
                }
            }
        }
        return list;
    }

    /** Full payslip for display, or null if it does not exist. */
    public PayslipDetail getDetail(int payslipId) throws SQLException {
        String sql = "SELECT e.employee_no, e.first_name, e.last_name, d.department_name, "
                   + "jp.position_title, pp.period_start, pp.period_end, p.basic_pay, p.overtime_pay "
                   + "FROM PAYSLIPS p "
                   + "INNER JOIN EMPLOYEES e ON p.employee_id = e.employee_id "
                   + "INNER JOIN DEPARTMENTS d ON e.department_id = d.department_id "
                   + "INNER JOIN JOB_POSITIONS jp ON e.position_id = jp.position_id "
                   + "INNER JOIN PAY_PERIODS pp ON p.period_id = pp.period_id "
                   + "WHERE p.payslip_id = ?";
        String dedSql = "SELECT dt.deduction_name, pd.amount "
                      + "FROM PAYSLIP_DEDUCTIONS pd "
                      + "INNER JOIN DEDUCTION_TYPES dt ON pd.deduction_type_id = dt.deduction_type_id "
                      + "WHERE pd.payslip_id = ? ORDER BY dt.deduction_name";
        DateTimeFormatter f = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);

        try (Connection c = DBConnection.get()) {
            String title;
            String subtitle;
            double basic;
            double overtime;

            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, payslipId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return null;
                    title = rs.getString(3) + ", " + rs.getString(2) + " - " + rs.getString(1);
                    subtitle = rs.getString(4) + " - " + rs.getString(5) + " - "
                             + f.format(rs.getDate(6).toLocalDate()) + " to "
                             + f.format(rs.getDate(7).toLocalDate());
                    basic = rs.getBigDecimal(8).doubleValue();
                    overtime = rs.getBigDecimal(9).doubleValue();
                }
            }

            List<String> names = new ArrayList<>();
            List<Double> amounts = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement(dedSql)) {
                ps.setInt(1, payslipId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        names.add(rs.getString(1));
                        amounts.add(rs.getBigDecimal(2).doubleValue());
                    }
                }
            }

            double[] amountArray = new double[amounts.size()];
            for (int i = 0; i < amountArray.length; i++) {
                amountArray[i] = amounts.get(i);
            }
            return new PayslipDetail(title, subtitle, basic, overtime,
                    names.toArray(new String[0]), amountArray);
        }
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