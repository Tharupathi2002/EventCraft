package com.eventcraft.taskschedule.repository;

import com.eventcraft.taskschedule.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByEventIdOrderByDeadlineAsc(Long eventId);

    List<Task> findByEventIdAndStatusOrderByDeadlineAsc(Long eventId, String status);

    List<Task> findByEventIdAndAssignedToOrderByDeadlineAsc(Long eventId, Long assignedTo);

    List<Task> findByEventIdAndStatusAndAssignedToOrderByDeadlineAsc(Long eventId, String status, Long assignedTo);

    void deleteByEventId(Long eventId);
}
