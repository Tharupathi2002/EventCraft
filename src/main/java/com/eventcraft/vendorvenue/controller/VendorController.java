package com.eventcraft.vendorvenue.controller;

import com.eventcraft.vendorvenue.entity.Vendor;
import com.eventcraft.vendorvenue.service.VendorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/vendor-venue/vendors")
public class VendorController {

    private final VendorService vendorService;

    @Autowired
    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    @GetMapping
    public String listVendors(@RequestParam(value = "keyword", required = false) String keyword,
                              @RequestParam(value = "serviceType", required = false) String serviceType,
                              Model model) {
        model.addAttribute("vendors", vendorService.searchVendors(keyword, serviceType));
        model.addAttribute("keyword", keyword);
        model.addAttribute("serviceType", serviceType);
        return "vendorvenue/vendors/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("vendor", new Vendor());
        model.addAttribute("pageTitle", "Add New Vendor");
        return "vendorvenue/vendors/form";
    }

    @PostMapping("/save")
    public String saveVendor(@Valid @ModelAttribute("vendor") Vendor vendor,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", vendor.getVendorId() == null ? "Add New Vendor" : "Edit Vendor");
            return "vendorvenue/vendors/form";
        }

        vendorService.saveVendor(vendor);
        redirectAttributes.addFlashAttribute("successMessage", "Vendor saved successfully!");
        return "redirect:/vendor-venue/vendors";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        return vendorService.getVendorById(id).map(vendor -> {
            model.addAttribute("vendor", vendor);
            model.addAttribute("pageTitle", "Edit Vendor - " + vendor.getBusinessName());
            return "vendorvenue/vendors/form";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage", "Vendor not found with ID: " + id);
            return "redirect:/vendor-venue/vendors";
        });
    }

    @GetMapping("/detail/{id}")
    public String showDetail(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        return vendorService.getVendorById(id).map(vendor -> {
            model.addAttribute("vendor", vendor);
            return "vendorvenue/vendors/detail";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage", "Vendor not found with ID: " + id);
            return "redirect:/vendor-venue/vendors";
        });
    }

    @GetMapping("/delete/{id}")
    public String deleteVendor(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            vendorService.deleteVendor(id);
            redirectAttributes.addFlashAttribute("successMessage", "Vendor profile deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not delete vendor: " + e.getMessage());
        }
        return "redirect:/vendor-venue/vendors";
    }
}
