package com.eventcraft.budgetexpense.repository;

import com.eventcraft.budgetexpense.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByEvent_HostUsernameOrderByCreatedDateDesc(String hostUsername);

    Optional<Budget> findByEvent_EventId(Long eventId);

    boolean existsByEvent_EventId(Long eventId);
}
