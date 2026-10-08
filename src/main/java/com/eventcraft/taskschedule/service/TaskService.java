package com.eventcraft.taskschedule.service;

import com.eventcraft.taskschedule.dao.TaskDAO;
import com.eventcraft.taskschedule.dto.CreateTaskRequest;
import com.eventcraft.taskschedule.dto.UpdateTaskRequest;
import com.eventcraft.taskschedule.exception.ResourceNotFoundException;
import com.eventcraft.taskschedule.exception.ValidationException;
import com.eventcraft.taskschedule.model.Task;
import com.eventcraft.taskschedule.model.TaskMetrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * TaskService handles business rules and validations for Task Management.
 */
@Service
public class TaskService {

    private final TaskDAO taskDAO;
    private static final List<String> VALID_PRIORITIES = Arrays.asList("Low", "Medium", "High");
    private static final List<String> VALID_STATUSES = Arrays.asList("Pending", "In Progress", "Completed");

    @Autowired
    public TaskService(TaskDAO taskDAO) {
        this.taskDAO = taskDAO;
    }

    public List<Task> getAllTasks(Integer eventId, String status, Integer assigneeId) {
        return taskDAO.findAll(eventId, status, assigneeId);
    }

    public Task getTaskById(int taskId) {
        return taskDAO.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task with ID " + taskId + " not found."));
    }

    public Task createTask(CreateTaskRequest req) {
        // 1. Verify Event exists
        if (!taskDAO.eventExists(req.getEventId())) {
            throw new ValidationException("Invalid Event ID: Event does not exist.");
        }

        // 2. Verify Creator exists
        if (!taskDAO.userExists(req.getCreatedBy())) {
            throw new ValidationException("Invalid CreatedBy User ID: User does not exist.");
        }

        // 3. Verify Assignee exists if assigned
        if (req.getAssignedTo() != null && !taskDAO.userExists(req.getAssignedTo())) {
            throw new ValidationException("Invalid Assignee User ID: Selected collaborator does not exist.");
        }

        // 4. Validate Priority
        String priority = req.getPriority() != null ? req.getPriority().trim() : "Medium";
        if (!VALID_PRIORITIES.contains(priority)) {
            throw new ValidationException("Invalid Priority. Allowed values: Low, Medium, High.");
        }

        // 5. Validate Deadline (cannot be in the past)
        if (req.getDeadline() == null || req.getDeadline().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Invalid Deadline: Deadline must be set to a future date and time.");
        }

        Task task = new Task();
        task.setEventId(req.getEventId());
        task.setTitle(req.getTitle().trim());
        task.setDescription(req.getDescription());
        task.setDeadline(req.getDeadline());
        task.setPriority(priority);
        task.setStatus("Pending");
        task.setProgress(0);
        task.setAssignedTo(req.getAssignedTo());
        task.setCreatedBy(req.getCreatedBy());

        return taskDAO.create(task);
    }

    public Task updateTask(int taskId, UpdateTaskRequest req) {
        Task existing = getTaskById(taskId);

        if (req.getTitle() != null && !req.getTitle().trim().isEmpty()) {
            existing.setTitle(req.getTitle().trim());
        }

        if (req.getDescription() != null) {
            existing.setDescription(req.getDescription());
        }

        if (req.getDeadline() != null) {
            existing.setDeadline(req.getDeadline());
        }

        if (req.getPriority() != null) {
            String p = req.getPriority().trim();
            if (!VALID_PRIORITIES.contains(p)) {
                throw new ValidationException("Invalid Priority. Allowed: Low, Medium, High.");
            }
            existing.setPriority(p);
        }

        if (req.getAssignedTo() != null) {
            if (!taskDAO.userExists(req.getAssignedTo())) {
                throw new ValidationException("Invalid Assignee User ID: User does not exist.");
            }
            existing.setAssignedTo(req.getAssignedTo());
        }

        // Handle Progress & Auto-Status logic
        if (req.getProgress() != null) {
            int prog = req.getProgress();
            if (prog < 0 || prog > 100) {
                throw new ValidationException("Progress must be between 0 and 100.");
            }
            existing.setProgress(prog);

            if (prog == 100) {
                existing.setStatus("Completed");
            } else if (prog > 0 && "Pending".equalsIgnoreCase(existing.getStatus())) {
                existing.setStatus("In Progress");
            }
        }

        if (req.getStatus() != null) {
            String s = req.getStatus().trim();
            if (!VALID_STATUSES.contains(s)) {
                throw new ValidationException("Invalid Status. Allowed: Pending, In Progress, Completed.");
            }
            existing.setStatus(s);
            if ("Completed".equalsIgnoreCase(s) && existing.getProgress() < 100) {
                existing.setProgress(100);
            }
        }

        boolean updated = taskDAO.update(existing);
        if (!updated) {
            throw new RuntimeException("Failed to update task with ID " + taskId);
        }

        return getTaskById(taskId);
    }

    public void deleteTask(int taskId) {
        // Ensure task exists before deletion
        getTaskById(taskId);
        taskDAO.delete(taskId);
    }

    public TaskMetrics getTaskMetrics(int eventId) {
        if (!taskDAO.eventExists(eventId)) {
            throw new ResourceNotFoundException("Event with ID " + eventId + " not found.");
        }
        return taskDAO.getMetrics(eventId);
    }
}
