package model;

import java.math.BigDecimal;

public class PayslipDeductions {
    private int payslipId;
    private int deductionTypeId;
    private BigDecimal amount = BigDecimal.ZERO;

    private String deductionName;   // for display only

    public PayslipDeductions() { }

    public PayslipDeductions(int payslipId, int deductionTypeId, BigDecimal amount) {
        this.payslipId = payslipId;
        this.deductionTypeId = deductionTypeId;
        this.amount = amount;
    }

    public int getPayslipId() { return payslipId; }
    public void setPayslipId(int payslipId) { this.payslipId = payslipId; }

    public int getDeductionTypeId() { return deductionTypeId; }
    public void setDeductionTypeId(int deductionTypeId) { this.deductionTypeId = deductionTypeId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDeductionName() { return deductionName; }
    public void setDeductionName(String deductionName) { this.deductionName = deductionName; }
}