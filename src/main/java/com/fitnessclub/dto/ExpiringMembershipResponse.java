package com.fitnessclub.dto;

import com.fitnessclub.model.Member;
import com.fitnessclub.model.Membership;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ExpiringMembershipResponse {
    private Long memberId;
    private String memberName;
    private String email;
    private String phoneNumber;
    private LocalDate membershipEndDate;
    private Long daysUntilExpiry;
    private String membershipType;

    public ExpiringMembershipResponse(Member member, Membership membership) {
        this.memberId = member.getId();
        this.memberName = member.getFirstName() + " " + member.getLastName();
        this.email = member.getEmail();
        this.phoneNumber = member.getPhoneNumber();
        this.membershipEndDate = membership.getEndDate();
        this.daysUntilExpiry = ChronoUnit.DAYS.between(LocalDate.now(), membership.getEndDate());
        this.membershipType = membership.getType().name();
    }

    // Геттеры
    public Long getMemberId() { return memberId; }
    public String getMemberName() { return memberName; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public LocalDate getMembershipEndDate() { return membershipEndDate; }
    public Long getDaysUntilExpiry() { return daysUntilExpiry; }
    public String getMembershipType() { return membershipType; }
}