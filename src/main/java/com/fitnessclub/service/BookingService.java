package com.fitnessclub.service;

import com.fitnessclub.model.Booking;
import com.fitnessclub.model.Member;
import com.fitnessclub.model.WorkoutClass;
import com.fitnessclub.repository.BookingRepository;
import com.fitnessclub.repository.MemberRepository;
import com.fitnessclub.repository.WorkoutClassRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final MemberRepository memberRepository;
    private final WorkoutClassRepository workoutClassRepository;
    private final MembershipService membershipService;

    @Autowired
    public BookingService(BookingRepository bookingRepository,
                          MemberRepository memberRepository,
                          WorkoutClassRepository workoutClassRepository,
                          MembershipService membershipService) {
        this.bookingRepository = bookingRepository;
        this.memberRepository = memberRepository;
        this.workoutClassRepository = workoutClassRepository;
        this.membershipService = membershipService;
    }

    @Transactional
    public Booking createBooking(Long memberId, Long classId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        WorkoutClass workoutClass = workoutClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Workout class not found"));

        // Проверка активной подписки
        if (!membershipService.hasActiveMembership(member)) {
            throw new RuntimeException("Member does not have an active membership");
        }

        // Проверка свободных мест
        if (!workoutClass.hasAvailableSpots()) {
            throw new RuntimeException("No available spots in this class");
        }

        // Проверка, что участник уже не записан
        if (bookingRepository.existsByMemberAndWorkoutClass(member, workoutClass)) {
            throw new RuntimeException("Member is already booked for this class");
        }

        // Проверка, что занятие еще не прошло
        if (!workoutClass.isUpcoming()) {
            throw new RuntimeException("Cannot book for past classes");
        }

        Booking booking = new Booking();
        booking.setMember(member);
        booking.setWorkoutClass(workoutClass);

        return bookingRepository.save(booking);
    }

    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.canBeCancelled()) {
            throw new RuntimeException("Booking cannot be cancelled. Minimum 2 hours notice required.");
        }

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    public List<Booking> getMemberBookings(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        return bookingRepository.findByMember(member);
    }

    public List<Booking> getClassBookings(Long classId) {
        WorkoutClass workoutClass = workoutClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Workout class not found"));
        return bookingRepository.findByWorkoutClass(workoutClass);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }
}