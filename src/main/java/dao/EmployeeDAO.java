package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Employee;

public class EmployeeDAO {

    public static final String PASSWORD_SUFFIX = "123";

    private static final String SELECT =
        "SELECT e.employee_id, e.employee_no, e.first_name, e.last_name, e.contact_no, "
      + "e.email, e.address, e.date_hired, e.department_id, e.position_id, e.status, "
      + "d.department_name, p.position_title, p.monthly_salary "
      + "FROM EMPLOYEES e "
      + "JOIN DEPARTMENTS d ON e.department_id = d.department_id "
      + "JOIN JOB_POSITIONS p ON e.position_id = p.position_id ";

    public static String defaultPassword(String lastName) {
        return lastName.replaceAll("\\s+", "") + PASSWORD_SUFFIX;
    }

    public static String formatEmployeeNo(int employeeId) {
        return String.format("EMP-%03d", employeeId);
    }

    public List<Employee> findAll() throws SQLException {
        return query(SELECT + "ORDER BY e.last_name, e.first_name");
    }

    public List<Employee> search(String keyword) throws SQLException {
        String like = "%" + keyword.trim().toLowerCase() + "%";
        return query(SELECT
                + "WHERE LOWER(e.first_name) LIKE ? OR LOWER(e.last_name) LIKE ? "
                + "OR LOWER(e.employee_no) LIKE ? ORDER BY e.last_name, e.first_name",
                like, like, like);
    }

    public Employee findById(int id) throws SQLException {
        List<Employee> list = query(SELECT + "WHERE e.employee_id = ?", id);
        return list.isEmpty() ? null : list.get(0);
    }

    public int insert(Employee e) throws SQLException {
        String sql = "INSERT INTO EMPLOYEES (employee_no, first_name, last_name, contact_no, "
                   + "email, address, date_hired, department_id, position_id, status) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        e.setEmployeeNo("T" + Long.toString(System.nanoTime(), 36));
        try (Connection c = DBConnection.get()) {
            c.setAutoCommit(false);
            try {
                int newId;
                try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    bind(ps, e);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        newId = keys.getInt(1);
                    }
                }
                String employeeNo = formatEmployeeNo(newId);
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE EMPLOYEES SET employee_no = ? WHERE employee_id = ?")) {
                    ps.setString(1, employeeNo);
                    ps.setInt(2, newId);
                    ps.executeUpdate();
                }
                e.setEmployeeId(newId);
                e.setEmployeeNo(employeeNo);
                createAccount(c, e, newId);
                c.commit();
                return newId;
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    public void update(Employee e) throws SQLException {
        String sql = "UPDATE EMPLOYEES SET employee_no = ?, first_name = ?, last_name = ?, "
                   + "contact_no = ?, email = ?, address = ?, date_hired = ?, "
                   + "department_id = ?, position_id = ?, status = ? WHERE employee_id = ?";
        String userSql = "UPDATE USERS SET full_name = ?, status = ? WHERE employee_id = ?";
        try (Connection c = DBConnection.get()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    bind(ps, e);
                    ps.setInt(11, e.getEmployeeId());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement(userSql)) {
                    ps.setString(1, e.getFullName());
                    ps.setString(2, e.getStatus());
                    ps.setInt(3, e.getEmployeeId());
                    ps.executeUpdate();
                }
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    public void setStatus(int employeeId, String status) throws SQLException {
        try (Connection c = DBConnection.get()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE EMPLOYEES SET status = ? WHERE employee_id = ?")) {
                    ps.setString(1, status);
                    ps.setInt(2, employeeId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE USERS SET status = ? WHERE employee_id = ?")) {
                    ps.setString(1, status);
                    ps.setInt(2, employeeId);
                    ps.executeUpdate();
                }
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    private void createAccount(Connection c, Employee e, int employeeId) throws SQLException {
        String sql = "INSERT INTO USERS (username, password_hash, full_name, user_role, status, employee_id) "
                   + "VALUES (?, ?, ?, 'EMPLOYEE', ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, e.getEmployeeNo());
            ps.setString(2, UserDAO.sha256(defaultPassword(e.getLastName())));
            ps.setString(3, e.getFullName());
            ps.setString(4, e.getStatus() == null ? "ACTIVE" : e.getStatus());
            ps.setInt(5, employeeId);
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, Employee e) throws SQLException {
        ps.setString(1, e.getEmployeeNo());
        ps.setString(2, e.getFirstName());
        ps.setString(3, e.getLastName());
        ps.setString(4, nullIfBlank(e.getContactNo()));
        ps.setString(5, nullIfBlank(e.getEmail()));
        ps.setString(6, nullIfBlank(e.getAddress()));
        ps.setDate(7, Date.valueOf(e.getDateHired()));
        ps.setInt(8, e.getDepartmentId());
        ps.setInt(9, e.getPositionId());
        ps.setString(10, e.getStatus() == null ? "ACTIVE" : e.getStatus());
    }

    private String nullIfBlank(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private List<Employee> query(String sql, Object... params) throws SQLException {
        List<Employee> list = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    private Employee map(ResultSet rs) throws SQLException {
        Employee e = new Employee();
        e.setEmployeeId(rs.getInt("employee_id"));
        e.setEmployeeNo(rs.getString("employee_no"));
        e.setFirstName(rs.getString("first_name"));
        e.setLastName(rs.getString("last_name"));
        e.setContactNo(rs.getString("contact_no"));
        e.setEmail(rs.getString("email"));
        e.setAddress(rs.getString("address"));
        e.setDateHired(rs.getDate("date_hired").toLocalDate());
        e.setDepartmentId(rs.getInt("department_id"));
        e.setPositionId(rs.getInt("position_id"));
        e.setStatus(rs.getString("status"));
        e.setDepartmentName(rs.getString("department_name"));
        e.setPositionTitle(rs.getString("position_title"));
        e.setMonthlySalary(rs.getBigDecimal("monthly_salary"));
        return e;
    }
}