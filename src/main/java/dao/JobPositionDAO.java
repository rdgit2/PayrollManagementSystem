package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.JobPosition;

public class JobPositionDAO {

    public List<JobPosition> findAll() throws SQLException {
        List<JobPosition> list = new ArrayList<>();
        String sql = "SELECT position_id, position_title, monthly_salary "
                   + "FROM JOB_POSITIONS ORDER BY position_title";
        try (Connection c = DBConnection.get();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new JobPosition(rs.getInt(1), rs.getString(2), rs.getBigDecimal(3)));
            }
        }
        return list;
    }

    public void insert(JobPosition p) throws SQLException {
        String sql = "INSERT INTO JOB_POSITIONS (position_title, monthly_salary) VALUES (?, ?)";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getPositionTitle());
            ps.setBigDecimal(2, p.getMonthlySalary());
            ps.executeUpdate();
        }
    }

    public void update(JobPosition p) throws SQLException {
        String sql = "UPDATE JOB_POSITIONS SET position_title = ?, monthly_salary = ? WHERE position_id = ?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getPositionTitle());
            ps.setBigDecimal(2, p.getMonthlySalary());
            ps.setInt(3, p.getPositionId());
            ps.executeUpdate();
        }
    }

    public void delete(int positionId) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM JOB_POSITIONS WHERE position_id = ?")) {
            ps.setInt(1, positionId);
            ps.executeUpdate();
        }
    }
}