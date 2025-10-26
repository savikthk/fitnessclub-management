package com.fitnessclub.service;

import com.fitnessclub.model.Member;
import com.fitnessclub.model.Membership;
import com.fitnessclub.repository.MembershipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class MembershipService {
    private final MembershipRepository membershipRepository;

    @Autowired
    public MembershipService(MembershipRepository membershipRepository) {
        this.membershipRepository = membershipRepository;
    }

    public boolean hasActiveMembership(Member member) {
        Optional<Membership> latestMembership = membershipRepository
                .findFirstByMemberOrderByEndDateDesc(member);

        return latestMembership.isPresent() && latestMembership.get().isActive();
    }

    public Membership createMembership(Membership membership) {
        return membershipRepository.save(membership);
    }

    public List<Membership> getMemberMemberships(Long memberId) {
        Member member = new Member();
        member.setId(memberId);
        return membershipRepository.findByMember(member);
    }

    public Optional<Membership> getCurrentMembership(Member member) {
        return membershipRepository.findFirstByMemberOrderByEndDateDesc(member);
    }
}