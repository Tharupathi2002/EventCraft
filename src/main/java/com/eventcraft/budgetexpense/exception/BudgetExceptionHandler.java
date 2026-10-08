package com.eventcraft.budgetexpense.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Sends the host back to their budget list (with a message) when a budget/expense/event
 * doesn't exist or isn't theirs. Scoped to the budget controllers only, so it doesn't clash
 * with the event-planning module's JSON error handler.
 */
@ControllerAdvice(basePackages = "com.eventcraft.budgetexpense.controller")
public class BudgetExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/budgets";
    }
}
