package se.it25102007.budgetmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.it25102007.budgetmanagement.model.Budget;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
}
