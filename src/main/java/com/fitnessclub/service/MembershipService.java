package com.fitnessclub.service;

import com.fitnessclub.dto.ExpiringMembershipResponse;
import com.fitnessclub.dto.FinancialReport;
import com.fitnessclub.dto.MembershipResponse;
import com.fitnessclub.model.Member;
import com.fitnessclub.model.Membership;
import com.fitnessclub.repository.MemberRepository;
import com.fitnessclub.repository.MembershipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class MembershipService {
    private final MembershipRepository membershipRepository;
    private final MemberRepository memberRepository;

    @Autowired
    public MembershipService(MembershipRepository membershipRepository, MemberRepository memberRepository) {
        this.membershipRepository = membershipRepository;
        this.memberRepository = memberRepository;
    }

    // Бизнес-операция 2: Автоматическое продление абонемента
    public MembershipResponse extendMembership(Long memberId, Integer monthsToExtend) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + memberId));

        Membership currentMembership = membershipRepository
                .findFirstByMemberOrderByEndDateDesc(member)
                .orElseThrow(() -> new RuntimeException("No membership found for member"));

        LocalDate newEndDate;
        if (currentMembership.isActive()) {
            newEndDate = currentMembership.getEndDate().plusMonths(monthsToExtend);
        } else {
            newEndDate = LocalDate.now().plusMonths(monthsToExtend);
        }

        Membership extendedMembership = new Membership();
        extendedMembership.setMember(member);
        extendedMembership.setStartDate(LocalDate.now());
        extendedMembership.setEndDate(newEndDate);
        extendedMembership.setType(currentMembership.getType());
        extendedMembership.setPrice(currentMembership.getPrice() * monthsToExtend);

        Membership savedMembership = membershipRepository.save(extendedMembership);
        return new MembershipResponse(savedMembership);
    }

    // Бизнес-операция 3: Генерация финансового отчета
    public FinancialReport generateFinancialReport(LocalDate startDate, LocalDate endDate) {
        List<Membership> membershipsInPeriod = membershipRepository.findByStartDateBetween(startDate, endDate);

        Double totalRevenue = membershipsInPeriod.stream()
                .mapToDouble(Membership::getPrice)
                .sum();

        Map<String, Double> revenueByType = membershipsInPeriod.stream()
                .collect(Collectors.groupingBy(
                        membership -> membership.getType().name(),
                        Collectors.summingDouble(Membership::getPrice)
                ));

        return new FinancialReport(startDate, endDate, totalRevenue, revenueByType, membershipsInPeriod.size());
    }

    // Бизнес-операция 4: Поиск клиентов с истекающим абонементом
    public List<ExpiringMembershipResponse> findExpiringMemberships(Integer daysThreshold) {
        LocalDate thresholdDate = LocalDate.now().plusDays(daysThreshold);

        List<Member> allMembers = memberRepository.findAll();

        return allMembers.stream()
                .map(member -> membershipRepository.findFirstByMemberOrderByEndDateDesc(member))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(membership -> membership.isActive() &&
                        !membership.getEndDate().isAfter(thresholdDate) &&
                        !membership.getEndDate().isBefore(LocalDate.now()))
                .map(membership -> new ExpiringMembershipResponse(membership.getMember(), membership))
                .collect(Collectors.toList());
    }

    public boolean hasActiveMembership(Member member) {
        Optional<Membership> latestMembership = membershipRepository
                .findFirstByMemberOrderByEndDateDesc(member);
        return latestMembership.isPresent() && latestMembership.get().isActive();
    }

    public Membership createMembership(Membership membership) {
        return membershipRepository.save(membership);
    }

    public List<MembershipResponse> getMemberMemberships(Long memberId) {
        Member member = new Member();
        member.setId(memberId);
        return membershipRepository.findByMember(member)
                .stream()
                .map(MembershipResponse::new)
                .collect(Collectors.toList());
    }

    public Optional<Membership> getCurrentMembership(Member member) {
        return membershipRepository.findFirstByMemberOrderByEndDateDesc(member);
    }

    public MembershipResponse updateMembership(Long membershipId, String type, String endDate, Double price) {
        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new RuntimeException("Membership not found with id: " + membershipId));

        if (type != null) {
            try {
                Membership.MembershipType typeEnum = Membership.MembershipType.valueOf(type.toUpperCase());
                membership.setType(typeEnum);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid membership type: " + type);
            }
        }

        if (endDate != null) {
            LocalDate newEndDate = LocalDate.parse(endDate);
            membership.setEndDate(newEndDate);
        }

        if (price != null) {
            membership.setPrice(price);
        }

        Membership savedMembership = membershipRepository.save(membership);
        return new MembershipResponse(savedMembership);
    }

    public void deleteMembership(Long membershipId) {
        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new RuntimeException("Membership not found with id: " + membershipId));
        membershipRepository.delete(membership);
    }
}