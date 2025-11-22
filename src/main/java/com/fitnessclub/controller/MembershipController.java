package com.fitnessclub.controller;

import com.fitnessclub.dto.ExpiringMembershipResponse;
import com.fitnessclub.dto.FinancialReport;
import com.fitnessclub.dto.MembershipResponse;
import com.fitnessclub.model.Membership;
import com.fitnessclub.service.MembershipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/memberships")
public class MembershipController {
    private final MembershipService membershipService;

    @Autowired
    public MembershipController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    // Бизнес-операция 2: Продление абонемента
    @PostMapping("/{memberId}/extend")
    public ResponseEntity<?> extendMembership(
            @PathVariable Long memberId,
            @RequestParam Integer months) {

        try {
            MembershipResponse extendedMembership = membershipService.extendMembership(memberId, months);
            return ResponseEntity.ok(extendedMembership);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Бизнес-операция 3: Финансовый отчет
    @GetMapping("/financial-report")
    public FinancialReport generateFinancialReport(
            @RequestParam String startDate,
            @RequestParam String endDate) {

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        return membershipService.generateFinancialReport(start, end);
    }

    // Бизнес-операция 4: Поиск истекающих абонементов
    @GetMapping("/expiring")
    public List<ExpiringMembershipResponse> findExpiringMemberships(
            @RequestParam(defaultValue = "7") Integer days) {

        return membershipService.findExpiringMemberships(days);
    }

    @PostMapping
    public Membership createMembership(@RequestBody Membership membership) {
        return membershipService.createMembership(membership);
    }

    @GetMapping("/member/{memberId}")
    public List<MembershipResponse> getMemberMemberships(@PathVariable Long memberId) {
        return membershipService.getMemberMemberships(memberId);
    }
}