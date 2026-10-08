package com.eventcraft.budgetexpense.service;

import com.eventcraft.budgetexpense.dto.BudgetForm;
import com.eventcraft.budgetexpense.dto.BudgetSummary;
import com.eventcraft.budgetexpense.dto.CategoryTotal;
import com.eventcraft.budgetexpense.dto.PaymentSummary;
import com.eventcraft.budgetexpense.entity.Budget;
import com.eventcraft.budgetexpense.exception.ResourceNotFoundException;
import com.eventcraft.budgetexpense.repository.BudgetRepository;
import com.eventcraft.budgetexpense.repository.ExpenseRepository;
import com.eventcraft.eventplanning.entity.Event;
import com.eventcraft.eventplanning.entity.EventStatus;
import com.eventcraft.eventplanning.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Business logic for budgets: CRUD (scoped to the logged-in host's own events) plus the
 * calculations behind the dashboard (remaining balance, spending by category, overspend
 * warning, paid/unpaid totals).
 *
 * Every method that takes a hostUsername only ever returns or changes budgets whose event is
 * hosted by that user; anything else is reported as "not found" so another host's budget ids
 * cannot be probed.
 */
@Service
@Transactional
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final EventRepository eventRepository;

    public BudgetService(BudgetRepository budgetRepository,
                         ExpenseRepository expenseRepository,
                         EventRepository eventRepository) {
        this.budgetRepository = budgetRepository;
        this.expenseRepository = expenseRepository;
        this.eventRepository = eventRepository;
    }

    // ---------- lookups ----------

    @Transactional(readOnly = true)
    public List<Budget> findForHost(String hostUsername) {
        return budgetRepository.findByEvent_HostUsernameOrderByCreatedDateDesc(hostUsername);
    }

    /** Loads a budget only if its event belongs to this host; otherwise behaves as not found. */
    @Transactional(readOnly = true)
    public Budget findOwnedOrThrow(Long budgetId, String hostUsername) {
        return budgetRepository.findById(budgetId)
                .filter(b -> isHostedBy(b.getEvent(), hostUsername))
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found."));
    }

    /** The budget of one of this host's events, if it has one. */
    @Transactional(readOnly = true)
    public Optional<Budget> findOwnedByEvent(Long eventId, String hostUsername) {
        return budgetRepository.findByEvent_EventId(eventId)
                .filter(b -> isHostedBy(b.getEvent(), hostUsername));
    }

    /** Host's events that can still receive a budget (not cancelled, no budget yet), soonest first. */
    @Transactional(readOnly = true)
    public List<Event> findEventsAvailableForBudget(String hostUsername) {
        List<Event> result = new ArrayList<>();
        for (Event event : eventRepository.findByHostUsername(hostUsername)) {
            if (event.getStatus() != EventStatus.CANCELLED
                    && !budgetRepository.existsByEvent_EventId(event.getEventId())) {
                result.add(event);
            }
        }
        result.sort(Comparator.comparing(Event::getEventDate,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return result;
    }

    // ---------- commands ----------

    /** @throws IllegalStateException if the event already has a budget. */
    public Budget create(BudgetForm form, String hostUsername) {
        Event event = eventRepository.findById(form.getEventId())
                .filter(e -> isHostedBy(e, hostUsername))
                .orElseThrow(() -> new ResourceNotFoundException("Event not found."));
        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new IllegalStateException("A cancelled event cannot have a budget.");
        }
        if (budgetRepository.existsByEvent_EventId(event.getEventId())) {
            throw new IllegalStateException("This event already has a budget.");
        }
        return budgetRepository.save(new Budget(event, form.getTotalBudget()));
    }

    /** Only the amount can change; the event a budget belongs to is fixed. */
    public Budget update(Long budgetId, String hostUsername, BigDecimal totalBudget) {
        Budget existing = findOwnedOrThrow(budgetId, hostUsername);
        existing.setTotalBudget(totalBudget);
        return budgetRepository.save(existing);
    }

    public void delete(Long budgetId, String hostUsername) {
        budgetRepository.delete(findOwnedOrThrow(budgetId, hostUsername));
    }

    /**
     * Called by the event-planning module when an event is deleted, so the budget (and its
     * expenses, via cascade) don't block the delete with a foreign-key violation.
     */
    public void deleteByEventId(Long eventId) {
        budgetRepository.findByEvent_EventId(eventId).ifPresent(budgetRepository::delete);
    }

    // ---------- dashboard calculations (callers must have verified ownership) ----------

    @Transactional(readOnly = true)
    public BudgetSummary buildSummary(Long budgetId) {
        Budget budget = findByIdOrThrow(budgetId);
        BigDecimal totalSpent = expenseRepository.sumAmountByBudgetId(budgetId);
        return new BudgetSummary(budget, totalSpent);
    }

    /**
     * Hook for the Dashboard &amp; Reports module: spent / remaining / over-budget for an event,
     * or empty if the event has no budget yet. Not host-scoped, since reports are built by
     * trusted server-side code, not directly from user input.
     */
    @Transactional(readOnly = true)
    public Optional<BudgetSummary> summaryForEvent(Long eventId) {
        return budgetRepository.findByEvent_EventId(eventId)
                .map(b -> new BudgetSummary(b, expenseRepository.sumAmountByBudgetId(b.getId())));
    }

    @Transactional(readOnly = true)
    public List<CategoryTotal> buildCategoryTotals(Long budgetId) {
        Budget budget = findByIdOrThrow(budgetId);
        List<Object[]> rows = expenseRepository.sumAmountGroupedByCategory(budgetId);
        List<CategoryTotal> results = new ArrayList<>();
        for (Object[] row : rows) {
            String category = (String) row[0];
            BigDecimal spent = (BigDecimal) row[1];
            results.add(new CategoryTotal(category, spent, budget.getTotalBudget()));
        }
        results.sort((a, b) -> b.getSpent().compareTo(a.getSpent()));
        return results;
    }

    @Transactional(readOnly = true)
    public PaymentSummary buildPaymentSummary(Long budgetId) {
        BigDecimal totalPaid = expenseRepository.sumAmountByBudgetIdAndPaid(budgetId, true);
        BigDecimal totalUnpaid = expenseRepository.sumAmountByBudgetIdAndPaid(budgetId, false);
        long paidCount = expenseRepository.countByBudgetIdAndPaid(budgetId, true);
        long unpaidCount = expenseRepository.countByBudgetIdAndPaid(budgetId, false);
        return new PaymentSummary(totalPaid, totalUnpaid, paidCount, unpaidCount);
    }

    // ---------- helpers ----------

    @Transactional(readOnly = true)
    Budget findByIdOrThrow(Long id) {
        return budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found."));
    }

    private boolean isHostedBy(Event event, String hostUsername) {
        return event != null && hostUsername != null && hostUsername.equals(event.getHostUsername());
    }
}
