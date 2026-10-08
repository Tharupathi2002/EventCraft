package com.eventcraft.vendorvenue.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** The portal has no dashboard any more: /vendor-venue lands on the venue list. */
@Controller
public class VendorVenueHomeController {

    @GetMapping("/vendor-venue")
    public String home() {
        return "redirect:/vendor-venue/venues";
    }
}
