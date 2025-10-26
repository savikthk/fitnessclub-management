package com.fitnessclub.controller;

import com.fitnessclub.model.Booking;
import com.fitnessclub.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestParam Long memberId, @RequestParam Long classId) {
        try {
            Booking booking = bookingService.createBooking(memberId, classId);
            return ResponseEntity.ok(booking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id) {
        try {
            bookingService.cancelBooking(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/member/{memberId}")
    public List<Booking> getMemberBookings(@PathVariable Long memberId) {
        return bookingService.getMemberBookings(memberId);
    }

    @GetMapping("/class/{classId}")
    public List<Booking> getClassBookings(@PathVariable Long classId) {
        return bookingService.getClassBookings(classId);
    }

    @GetMapping
    public List<Booking> getAllBookings() {
        return bookingService.getAllBookings();
    }
}