package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.DeductionType;

public class DeductionTypeDAO {

    public List<DeductionType> findAll() throws SQLException {
        List<DeductionType> list = new ArrayList<>();
        String sql = "SELECT deduction_type_id, deduction_name, description "
                   + "FROM DEDUCTION_TYPES ORDER BY deduction_name";
        try (Connection c = DBConnection.get();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new DeductionType(rs.getInt(1), rs.getString(2), rs.getString(3)));
            }
        }
        return list;
    }

    public void insert(DeductionType d) throws SQLException {
        String sql = "INSERT INTO DEDUCTION_TYPES (deduction_name, description) VALUES (?, ?)";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, d.getDeductionName());
            ps.setString(2, d.getDescription());
            ps.executeUpdate();
        }
    }

    public void update(DeductionType d) throws SQLException {
        String sql = "UPDATE DEDUCTION_TYPES SET deduction_name = ?, description = ? WHERE deduction_type_id = ?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, d.getDeductionName());
            ps.setString(2, d.getDescription());
            ps.setInt(3, d.getDeductionTypeId());
            ps.executeUpdate();
        }
    }

    /** Fails (SQL state 23503) if the type is already used on a payslip. */
    public void delete(int deductionTypeId) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM DEDUCTION_TYPES WHERE deduction_type_id = ?")) {
            ps.setInt(1, deductionTypeId);
            ps.executeUpdate();
        }
    }
}