package com.eventcraft.taskschedule.controller;

import com.eventcraft.taskschedule.dto.ApiResponse;
import com.eventcraft.taskschedule.dto.CreateTaskRequest;
import com.eventcraft.taskschedule.dto.UpdateTaskRequest;
import com.eventcraft.taskschedule.model.Task;
import com.eventcraft.taskschedule.model.TaskMetrics;
import com.eventcraft.taskschedule.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * TaskController exposes REST APIs for managing tasks in UC-17.
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * GET /api/tasks
     * Retrieve all tasks, optionally filtered by eventId, status, or assigneeId.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Task>>> getAllTasks(
            @RequestParam(required = false) Integer eventId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer assigneeId) {
        List<Task> tasks = taskService.getAllTasks(eventId, status, assigneeId);
        return ResponseEntity.ok(ApiResponse.ok("Tasks retrieved successfully", tasks));
    }

    /**
     * GET /api/tasks/{id}
     * Retrieve single task details by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Task>> getTaskById(@PathVariable("id") int taskId) {
        Task task = taskService.getTaskById(taskId);
        return ResponseEntity.ok(ApiResponse.ok("Task retrieved successfully", task));
    }

    /**
     * POST /api/tasks
     * Create a new task (validates deadline, event, and assignee).
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Task>> createTask(@Valid @RequestBody CreateTaskRequest req) {
        Task created = taskService.createTask(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Task created successfully", created));
    }

    /**
     * PUT /api/tasks/{id}
     * Update an existing task (handles progress updates & auto-complete status).
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Task>> updateTask(
            @PathVariable("id") int taskId,
            @Valid @RequestBody UpdateTaskRequest req) {
        Task updated = taskService.updateTask(taskId, req);
        return ResponseEntity.ok(ApiResponse.ok("Task updated successfully", updated));
    }

    /**
     * DELETE /api/tasks/{id}
     * Remove a task.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable("id") int taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.ok(ApiResponse.ok("Task with ID " + taskId + " deleted successfully."));
    }

    /**
     * GET /api/tasks/metrics/{eventId}
     * Returns task statistics for Member 6's Event Dashboard (UC-06).
     */
    @GetMapping("/metrics/{eventId}")
    public ResponseEntity<ApiResponse<TaskMetrics>> getTaskMetrics(@PathVariable("eventId") int eventId) {
        TaskMetrics metrics = taskService.getTaskMetrics(eventId);
        return ResponseEntity.ok(ApiResponse.ok("Task metrics retrieved successfully", metrics));
    }
}
