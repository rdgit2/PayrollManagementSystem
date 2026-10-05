/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author alfre
 */
public class Employee {
     private int employeeId;
    private int employeeNo;
    private String firstName;
    private String lastName;
    private String email;   
    private String address;
    private String date_hired;
    private int departmentId;
    private int positionId;
    private String status;
    
public Employee() { }

    public Employee(int employeeId, int employeeNo, String firstName, String lastName, String email,
                String address, String date_hired, int departmentId, int positionId, String status) {
        this.employeeId = employeeId;
        this.employeeNo = employeeNo;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.address = address;
        this.date_hired = date_hired;
        this.departmentId = departmentId;
        this.positionId = positionId;
        this.status = status;
    }
    
    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public int getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(int employeeNo) { this.employeeNo = employeeNo; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) {this.address = address; }
    
    public String getDateHIred() { return date_hired; }
    public void setDateHIred(String date_hired) {this.date_hired = date_hired; }
    
    public int getDepartmentId() { return departmentId; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }
    
    public int getPositionId() { return positionId; }
    public void setPositionId(int positionId) { this.positionId = positionId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
