package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.PayPeriod;

public class PayPeriodDAO {

    public List<PayPeriod> findAll() throws SQLException {
        List<PayPeriod> list = new ArrayList<>();
        String sql = "SELECT period_id, period_start, period_end, pay_date, status "
                   + "FROM PAY_PERIODS ORDER BY period_start DESC";
        try (Connection c = DBConnection.get();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public PayPeriod findOpen() throws SQLException {
        String sql = "SELECT period_id, period_start, period_end, pay_date, status FROM PAY_PERIODS "
                   + "WHERE status = 'OPEN' ORDER BY period_start DESC FETCH FIRST ROW ONLY";
        try (Connection c = DBConnection.get();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? map(rs) : null;
        }
    }

    public void insert(PayPeriod p) throws SQLException {
        String sql = "INSERT INTO PAY_PERIODS (period_start, period_end, pay_date, status) "
                   + "VALUES (?, ?, ?, 'OPEN')";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(p.getPeriodStart()));
            ps.setDate(2, Date.valueOf(p.getPeriodEnd()));
            ps.setDate(3, Date.valueOf(p.getPayDate()));
            ps.executeUpdate();
        }
    }

    public void close(int periodId) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE PAY_PERIODS SET status = 'CLOSED' WHERE period_id = ?")) {
            ps.setInt(1, periodId);
            ps.executeUpdate();
        }
    }

    private PayPeriod map(ResultSet rs) throws SQLException {
        PayPeriod p = new PayPeriod();
        p.setPeriodId(rs.getInt(1));
        p.setPeriodStart(rs.getDate(2).toLocalDate());
        p.setPeriodEnd(rs.getDate(3).toLocalDate());
        p.setPayDate(rs.getDate(4).toLocalDate());
        p.setStatus(rs.getString(5));
        return p;
    }
}