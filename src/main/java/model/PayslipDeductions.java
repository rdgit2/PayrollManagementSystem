/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author alfre
 */
public class PayslipDeductions {
    private int payslipId;
    private int deductionTypeId;
    private double amount;

    public PayslipDeductions() { }

    public PayslipDeductions(int payslipId, int deductionTypeId, double amount) {
        this.payslipId = payslipId;
        this.deductionTypeId = deductionTypeId;
        this.amount = amount;
    }

    public int getPayslipId() { return payslipId; }
    public void setPayslipId(int payslipId) { this.payslipId = payslipId; }

    public int getDeductionTypeId() { return deductionTypeId; }
    public void setDeductionTypeId(int deductionTypeId) { this.deductionTypeId = deductionTypeId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

}
