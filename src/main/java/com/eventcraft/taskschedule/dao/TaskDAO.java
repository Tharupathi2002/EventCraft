package com.eventcraft.taskschedule.dao;

import com.eventcraft.taskschedule.model.Task;
import com.eventcraft.taskschedule.model.TaskMetrics;

import java.util.List;
import java.util.Optional;

/**
 * TaskDAO Interface.
 * Implements the Data Access Object (DAO) Design Pattern
 * to abstract and decouple database interactions from business logic.
 */
public interface TaskDAO {
    List<Task> findAll(Integer eventId, String status, Integer assigneeId);
    Optional<Task> findById(int taskId);
    Task create(Task task);
    boolean update(Task task);
    boolean delete(int taskId);
    TaskMetrics getMetrics(int eventId);
    boolean userExists(int userId);
    boolean eventExists(int eventId);
}
