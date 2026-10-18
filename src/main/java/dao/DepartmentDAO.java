package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Department;

public class DepartmentDAO {

    public List<Department> findAll() throws SQLException {
        List<Department> list = new ArrayList<>();
        String sql = "SELECT department_id, department_name, description "
                   + "FROM DEPARTMENTS ORDER BY department_name";
        try (Connection c = DBConnection.get();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Department(rs.getInt(1), rs.getString(2), rs.getString(3)));
            }
        }
        return list;
    }

    public void insert(Department d) throws SQLException {
        String sql = "INSERT INTO DEPARTMENTS (department_name, description) VALUES (?, ?)";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, d.getDepartmentName());
            ps.setString(2, d.getDescription());
            ps.executeUpdate();
        }
    }

    public void update(Department d) throws SQLException {
        String sql = "UPDATE DEPARTMENTS SET department_name = ?, description = ? WHERE department_id = ?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, d.getDepartmentName());
            ps.setString(2, d.getDescription());
            ps.setInt(3, d.getDepartmentId());
            ps.executeUpdate();
        }
    }

    public void delete(int departmentId) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM DEPARTMENTS WHERE department_id = ?")) {
            ps.setInt(1, departmentId);
            ps.executeUpdate();
        }
    }
}   