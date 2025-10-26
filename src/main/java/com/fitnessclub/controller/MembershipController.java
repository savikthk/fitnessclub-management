package com.fitnessclub.controller;

import com.fitnessclub.model.Membership;
import com.fitnessclub.service.MembershipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/memberships")
public class MembershipController {
    private final MembershipService membershipService;

    @Autowired
    public MembershipController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @PostMapping
    public Membership createMembership(@RequestBody Membership membership) {
        return membershipService.createMembership(membership);
    }

    @GetMapping("/member/{memberId}")
    public List<Membership> getMemberMemberships(@PathVariable Long memberId) {
        return membershipService.getMemberMemberships(memberId);
    }
}