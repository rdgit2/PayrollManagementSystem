/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 *
 * @author alfre
 */
public class PayPeriod {
    private int periodId;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDate payDate;
    private String status;

    public PayPeriod() { }

    public PayPeriod(int perodId, LocalDate periodStart, LocalDate periodEnd,
            LocalDate payDate,String status) {
        this.periodId = periodId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.payDate = payDate;
        this.status = status;
    }
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

}
