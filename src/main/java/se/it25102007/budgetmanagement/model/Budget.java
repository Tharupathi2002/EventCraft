package se.it25102007.budgetmanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "budget")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "budget_id")
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @NotBlank(message = "Event name is required")
    @Column(name = "event_name", nullable = false, length = 150)
    private String eventName;

    @NotNull(message = "Total budget is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Total budget cannot be negative")
    @Column(name = "total_budget", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalBudget;

    @Column(name = "created_date")
    private LocalDate createdDate = LocalDate.now();

    @OneToMany(mappedBy = "budget", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Expense> expenses = new ArrayList<>();

    public Budget() {
    }

    public Budget(Long eventId, String eventName, BigDecimal totalBudget) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.totalBudget = totalBudget;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public BigDecimal getTotalBudget() {
        return totalBudget;
    }

    public void setTotalBudget(BigDecimal totalBudget) {
        this.totalBudget = totalBudget;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses;
    }
}
