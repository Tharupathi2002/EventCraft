package com.eventcraft.vendorvenue.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/vendor-venue/bookings")
public class BookingController {

    @GetMapping
    public String listBookings() {
        return "redirect:/vendor-venue/venues";
    }

    @GetMapping("/**")
    public String fallback() {
        return "redirect:/vendor-venue/venues";
    }
}
