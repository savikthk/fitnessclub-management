package com.fitnessclub.repository;

import com.fitnessclub.model.Member;
import com.fitnessclub.model.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {
    List<Membership> findByMember(Member member);
    Optional<Membership> findFirstByMemberOrderByEndDateDesc(Member member);
}