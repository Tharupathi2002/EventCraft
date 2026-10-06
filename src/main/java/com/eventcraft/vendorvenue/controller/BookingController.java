package com.eventcraft.vendorvenue.controller;

import com.eventcraft.vendorvenue.model.Booking;
import com.eventcraft.vendorvenue.service.BookingService;
import com.eventcraft.vendorvenue.service.VendorService;
import com.eventcraft.vendorvenue.service.VenueService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final VenueService venueService;
    private final VendorService vendorService;

    @Autowired
    public BookingController(BookingService bookingService, VenueService venueService, VendorService vendorService) {
        this.bookingService = bookingService;
        this.venueService = venueService;
        this.vendorService = vendorService;
    }

    @GetMapping
    public String listBookings(@RequestParam(value = "status", required = false) String status, Model model) {
        if (status != null && !status.trim().isEmpty()) {
            model.addAttribute("bookings", bookingService.getBookingsByStatus(status.toUpperCase()));
        } else {
            model.addAttribute("bookings", bookingService.getAllBookings());
        }
        model.addAttribute("selectedStatus", status);
        return "bookings/list";
    }

    @GetMapping("/new")
    public String showCreateForm(@RequestParam(value = "venueId", required = false) Long venueId,
                                 @RequestParam(value = "vendorId", required = false) Long vendorId,
                                 Model model) {
        Booking booking = new Booking();
        if (venueId != null) {
            venueService.getVenueById(venueId).ifPresent(v -> {
                booking.setVenueId(v.getVenueId());
                booking.setBookingType("VENUE");
                booking.setAmount(v.getRatePerDay());
            });
        } else if (vendorId != null) {
            vendorService.getVendorById(vendorId).ifPresent(v -> {
                booking.setVendorId(v.getVendorId());
                booking.setBookingType("VENDOR");
                booking.setAmount(v.getBasePrice());
            });
        }
        
        model.addAttribute("booking", booking);
        model.addAttribute("venues", venueService.getAllVenues());
        model.addAttribute("vendors", vendorService.getAllVendors());
        return "bookings/form";
    }

    @PostMapping("/save")
    public String saveBooking(@Valid @ModelAttribute("booking") Booking booking,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("venues", venueService.getAllVenues());
            model.addAttribute("vendors", vendorService.getAllVendors());
            return "bookings/form";
        }

        bookingService.createBooking(booking);
        redirectAttributes.addFlashAttribute("successMessage", "Booking request submitted successfully!");
        return "redirect:/bookings";
    }

    @PostMapping("/status")
    public String updateStatus(@RequestParam("bookingId") Long bookingId,
                               @RequestParam("status") String status,
                               RedirectAttributes redirectAttributes) {
        try {
            bookingService.updateBookingStatus(bookingId, status);
            redirectAttributes.addFlashAttribute("successMessage", "Booking #" + bookingId + " status updated to " + status + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating booking: " + e.getMessage());
        }
        return "redirect:/bookings";
    }

    @GetMapping("/delete/{id}")
    public String deleteBooking(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.deleteBooking(id);
            redirectAttributes.addFlashAttribute("successMessage", "Booking removed successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not delete booking: " + e.getMessage());
        }
        return "redirect:/bookings";
    }
}
