package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PayPeriod {
    private int periodId;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDate payDate;
    private String status;   
    public PayPeriod() { }

    public int getPeriodId() { return periodId; }
    public void setPeriodId(int periodId) { this.periodId = periodId; }

    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }

    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }

    public LocalDate getPayDate() { return payDate; }
    public void setPayDate(LocalDate payDate) { this.payDate = payDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("MMM d, yyyy");
        return periodStart.format(f) + " - " + periodEnd.format(f) + " (" + status + ")";
    }
}