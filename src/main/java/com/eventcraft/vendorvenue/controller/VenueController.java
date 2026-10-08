package com.eventcraft.vendorvenue.controller;

import com.eventcraft.vendorvenue.entity.Venue;
import com.eventcraft.vendorvenue.service.VenueService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/vendor-venue/venues")
public class VenueController {

    private final VenueService venueService;

    @Autowired
    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @GetMapping
    public String listVenues(@RequestParam(value = "keyword", required = false) String keyword,
                             @RequestParam(value = "city", required = false) String city,
                             Model model) {
        model.addAttribute("venues", venueService.searchVenues(keyword, city));
        model.addAttribute("keyword", keyword);
        model.addAttribute("city", city);
        return "vendorvenue/venues/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("venue", new Venue());
        model.addAttribute("pageTitle", "Add New Venue");
        return "vendorvenue/venues/form";
    }

    @PostMapping("/save")
    public String saveVenue(@Valid @ModelAttribute("venue") Venue venue,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", venue.getVenueId() == null ? "Add New Venue" : "Edit Venue");
            return "vendorvenue/venues/form";
        }

        venueService.saveVenue(venue);
        redirectAttributes.addFlashAttribute("successMessage", "Venue saved successfully!");
        return "redirect:/vendor-venue/venues";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        return venueService.getVenueById(id).map(venue -> {
            model.addAttribute("venue", venue);
            model.addAttribute("pageTitle", "Edit Venue - " + venue.getName());
            return "vendorvenue/venues/form";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage", "Venue not found with ID: " + id);
            return "redirect:/vendor-venue/venues";
        });
    }

    @GetMapping("/detail/{id}")
    public String showDetail(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        return venueService.getVenueById(id).map(venue -> {
            model.addAttribute("venue", venue);
            return "vendorvenue/venues/detail";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage", "Venue not found with ID: " + id);
            return "redirect:/vendor-venue/venues";
        });
    }

    @GetMapping("/delete/{id}")
    public String deleteVenue(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            venueService.deleteVenue(id);
            redirectAttributes.addFlashAttribute("successMessage", "Venue deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not delete venue: " + e.getMessage());
        }
        return "redirect:/vendor-venue/venues";
    }
}
