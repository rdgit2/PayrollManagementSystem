package model;

import java.math.BigDecimal;

public class JobPosition {
    private int positionId;
    private String positionTitle;
    private BigDecimal monthlySalary;

    public JobPosition() { }

    public JobPosition(int positionId, String positionTitle, BigDecimal monthlySalary) {
        this.positionId = positionId;
        this.positionTitle = positionTitle;
        this.monthlySalary = monthlySalary;
    }

    public int getPositionId() { return positionId; }
    public void setPositionId(int positionId) { this.positionId = positionId; }

    public String getPositionTitle() { return positionTitle; }
    public void setPositionTitle(String positionTitle) { this.positionTitle = positionTitle; }

    public BigDecimal getMonthlySalary() { return monthlySalary; }
    public void setMonthlySalary(BigDecimal monthlySalary) { this.monthlySalary = monthlySalary; }

    @Override public String toString() { return positionTitle; } // for combo boxes
}