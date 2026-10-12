package dao;

public class DaoTest {
    public static void main(String[] args) throws Exception {
        System.out.println("Login:       " + new UserDAO().login("admin", "admin123"));
        System.out.println("Employees:   " + new EmployeeDAO().findAll().size());
        System.out.println("Departments: " + new DepartmentDAO().findAll());
        System.out.println("Positions:   " + new JobPositionDAO().findAll());
        System.out.println("Deductions:  " + new DeductionTypeDAO().findAll());
        System.out.println("Open period: " + new PayPeriodDAO().findOpen());
        System.out.println("Stats:       " + new DashboardDAO().load());
    }
}