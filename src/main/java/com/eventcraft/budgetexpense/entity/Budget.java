package com.eventcraft.budgetexpense.entity;

import com.eventcraft.eventplanning.entity.Event;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * The budget for one event. Each event has at most one budget (unique event_id).
 * Remaining balance is never stored: it is always TotalBudget - sum(expenses),
 * calculated on read (see BudgetSummary).
 */
@Entity
@Table(name = "budget")
@Getter
@Setter
@NoArgsConstructor
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "budget_id")
    private Long id;

    /** Real link to the event-planning module's Event (replaces the old free-standing eventId). */
    @OneToOne(optional = false)
    @JoinColumn(name = "event_id", nullable = false, unique = true)
    private Event event;

    @NotNull(message = "Total budget is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Total budget cannot be negative")
    @Column(name = "total_budget", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalBudget;

    @Column(name = "created_date")
    private LocalDate createdDate = LocalDate.now();

    @OneToMany(mappedBy = "budget", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Expense> expenses = new ArrayList<>();

    public Budget(Event event, BigDecimal totalBudget) {
        this.event = event;
        this.totalBudget = totalBudget;
    }

    // Convenience accessors so the templates can keep using budget.eventName / budget.eventId.

    @Transient
    public Long getEventId() {
        return event == null ? null : event.getEventId();
    }

    @Transient
    public String getEventName() {
        return event == null ? null : event.getEventName();
    }

    @Transient
    public LocalDate getEventDate() {
        return event == null ? null : event.getEventDate();
    }
}
