package model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Employee {
    private int employeeId;
    private String employeeNo;
    private String firstName;
    private String lastName;
    private String contactNo;
    private String email;
    private String address;
    private LocalDate dateHired;
    private int departmentId;
    private int positionId;
    private String status;      // ACTIVE, INACTIVE

    // filled by joins for display only, not columns of EMPLOYEES
    private String departmentName;
    private String positionTitle;
    private BigDecimal monthlySalary;   // comes from JOB_POSITIONS

    public Employee() { }

    public Employee(int employeeId, String employeeNo, String firstName, String lastName,
                    String contactNo, String email, String address, LocalDate dateHired,
                    int departmentId, int positionId, String status) {
        this.employeeId = employeeId;
        this.employeeNo = employeeNo;
        this.firstName = firstName;
        this.lastName = lastName;
        this.contactNo = contactNo;
        this.email = email;
        this.address = address;
        this.dateHired = dateHired;
        this.departmentId = departmentId;
        this.positionId = positionId;
        this.status = status;
    }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getContactNo() { return contactNo; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDate getDateHired() { return dateHired; }
    public void setDateHired(LocalDate dateHired) { this.dateHired = dateHired; }

    public int getDepartmentId() { return departmentId; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }

    public int getPositionId() { return positionId; }
    public void setPositionId(int positionId) { this.positionId = positionId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getPositionTitle() { return positionTitle; }
    public void setPositionTitle(String positionTitle) { this.positionTitle = positionTitle; }

    public BigDecimal getMonthlySalary() { return monthlySalary; }
    public void setMonthlySalary(BigDecimal monthlySalary) { this.monthlySalary = monthlySalary; }

    public String getFullName() { return firstName + " " + lastName; }
}