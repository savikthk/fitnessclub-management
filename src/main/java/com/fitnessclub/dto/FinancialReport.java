package com.fitnessclub.dto;

import java.time.LocalDate;
import java.util.Map;

public class FinancialReport {
    private LocalDate startDate;
    private LocalDate endDate;
    private Double totalRevenue;
    private Map<String, Double> revenueByMembershipType;
    private Integer totalMembershipsSold;
    private String reportGeneratedAt;

    public FinancialReport(LocalDate startDate, LocalDate endDate, Double totalRevenue,
                           Map<String, Double> revenueByMembershipType, Integer totalMembershipsSold) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalRevenue = totalRevenue;
        this.revenueByMembershipType = revenueByMembershipType;
        this.totalMembershipsSold = totalMembershipsSold;
        this.reportGeneratedAt = java.time.LocalDateTime.now().toString();
    }

    // Геттеры
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public Double getTotalRevenue() { return totalRevenue; }
    public Map<String, Double> getRevenueByMembershipType() { return revenueByMembershipType; }
    public Integer getTotalMembershipsSold() { return totalMembershipsSold; }
    public String getReportGeneratedAt() { return reportGeneratedAt; }
}