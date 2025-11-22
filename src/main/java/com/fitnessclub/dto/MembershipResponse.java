package com.fitnessclub.dto;

import com.fitnessclub.model.Membership;
import java.time.LocalDate;

public class MembershipResponse {
    private Long id;
    private Long memberId;
    private String memberName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String type;
    private Double price;
    private boolean active;

    public MembershipResponse(Membership membership) {
        this.id = membership.getId();
        this.memberId = membership.getMember().getId();
        this.memberName = membership.getMember().getFirstName() + " " + membership.getMember().getLastName();
        this.startDate = membership.getStartDate();
        this.endDate = membership.getEndDate();
        this.type = membership.getType().name();
        this.price = membership.getPrice();
        this.active = membership.isActive();
    }

    // Геттеры
    public Long getId() { return id; }
    public Long getMemberId() { return memberId; }
    public String getMemberName() { return memberName; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getType() { return type; }
    public Double getPrice() { return price; }
    public boolean isActive() { return active; }
}