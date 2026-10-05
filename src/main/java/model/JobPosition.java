/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author alfre
 */
public class JobPosition {
    private int positionId;
    private String positionTitle;
    private double monthlySalary;

    public JobPosition() { }

    public JobPosition(int positionId, String positionTitle, double monthlySalary) {
        this.positionId = positionId;
        this.positionTitle = positionTitle;
        this.monthlySalary = monthlySalary;
    }

    public int getPositionId() { return positionId; }
    public void setPositionId(int positionId) { this.positionId = positionId; }

    public String getPositionTitle() { return positionTitle; }
    public void setPositionTitle(String positionTitle) { this.positionTitle = positionTitle; }

    public double getMonthlySalary() { return monthlySalary; }
    public void setMonthlySalary(double monthlySalary) { this.monthlySalary = monthlySalary; }

}
