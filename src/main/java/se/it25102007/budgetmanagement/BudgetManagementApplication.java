package se.it25102007.budgetmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Budget & Expense Management module (EventCraft).
 * Runs fully standalone: its own build, its own UI, its own database.
 */
@SpringBootApplication
public class BudgetManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(BudgetManagementApplication.class, args);
    }

}
