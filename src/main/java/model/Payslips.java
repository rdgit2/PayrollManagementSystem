/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.time.LocalDate;

/**
 *
 * @author alfre
 */
public class Payslips {
    private int payslipId;
    private int periodId;
    private int employeeId;
    private int processedBy;
    private double basicPay;
    private double overtimePay;
    private LocalDate dateGenerated;

    public Payslips() { }

    public Payslips(int payslipId, int periodId, int employeeId,int processedBy, 
            double basicPay, double overtimePay, LocalDate dateGenerated) {
        this.payslipId = payslipId;
        this.periodId = periodId;
        this.employeeId = employeeId;
        this.processedBy = processedBy;
        this.basicPay = basicPay;
        this.overtimePay = overtimePay;
        this.dateGenerated = dateGenerated;
    }
    public int getPayslipId() { return payslipId; }
    public void setPayslipId(int payslipId) { this.payslipId = payslipId; }

    public int getPeriodId() { return periodId; }
    public void setPeriodId(int periodId) { this.periodId = periodId; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public int getProcessedBy() { return processedBy; }
    public void setProcessedBy(int processedBy) { this.processedBy = processedBy; }

    public double getBasicPay() { return basicPay; }
    public void setBasicPay(double basicPay) { this.basicPay = basicPay; }

    public double getOvertimePay() { return overtimePay; }
    public void setOvertimePay(double overtimePay) { this.overtimePay = overtimePay; }

    public LocalDate getDateGenerated() { return dateGenerated; }
    public void setDateGenerated(LocalDate dateGenerated) { this.dateGenerated = dateGenerated; }

}
