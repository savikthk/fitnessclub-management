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

    @Transactional
    public Booking updateBooking(Long bookingId, Long newClassId, String status) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        boolean changed = false;

        if (newClassId != null) {
            WorkoutClass newClass = workoutClassRepository.findById(newClassId)
                    .orElseThrow(() -> new RuntimeException("Workout class not found"));

            if (!newClass.isUpcoming()) {
                throw new RuntimeException("Cannot move booking to past class");
            }
            if (!newClass.hasAvailableSpots()) {
                throw new RuntimeException("No available spots in target class");
            }
            if (bookingRepository.existsByMemberAndWorkoutClass(booking.getMember(), newClass)) {
                throw new RuntimeException("Member already booked for target class");
            }

            booking.setWorkoutClass(newClass);
            changed = true;
        }

        if (status != null) {
            try {
                Booking.BookingStatus newStatus = Booking.BookingStatus.valueOf(status.toUpperCase());
                booking.setStatus(newStatus);
                changed = true;
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException("Invalid status value");
            }
        }

        if (!changed) {
            throw new RuntimeException("Nothing to update");
        }

        return bookingRepository.save(booking);
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

    public Booking updateBooking(Long bookingId, Long newClassId, String newStatus) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Обновление класса, если передан новый
        if (newClassId != null && !newClassId.equals(booking.getWorkoutClass().getId())) {
            Member member = booking.getMember();
            if (!membershipService.hasActiveMembership(member)) {
                throw new RuntimeException("Member does not have an active membership");
            }

            WorkoutClass newClass = workoutClassRepository.findById(newClassId)
                    .orElseThrow(() -> new RuntimeException("Workout class not found"));

            if (!newClass.isUpcoming()) {
                throw new RuntimeException("Cannot rebook to a past class");
            }
            if (!newClass.hasAvailableSpots()) {
                throw new RuntimeException("No available spots in the new class");
            }
            if (bookingRepository.existsByMemberAndWorkoutClass(member, newClass)) {
                throw new RuntimeException("Member is already booked for this class");
            }
            booking.setWorkoutClass(newClass);
        }

        // Обновление статуса, если передан новый
        if (newStatus != null) {
            try {
                Booking.BookingStatus statusEnum = Booking.BookingStatus.valueOf(newStatus.toUpperCase());
                booking.setStatus(statusEnum);
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException("Invalid status value");
            }
        }

        return bookingRepository.save(booking);
    }
}