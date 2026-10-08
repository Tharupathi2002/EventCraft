package com.eventcraft.taskschedule.controller;

import com.eventcraft.auth.AuthSession;
import com.eventcraft.taskschedule.dto.ApiResponse;
import com.eventcraft.taskschedule.dto.AssigneeOption;
import com.eventcraft.taskschedule.dto.CreateTaskRequest;
import com.eventcraft.taskschedule.dto.TaskMetrics;
import com.eventcraft.taskschedule.dto.UpdateTaskRequest;
import com.eventcraft.taskschedule.entity.Task;
import com.eventcraft.taskschedule.service.TaskService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** REST API for tasks (UC-17). Host-only; see auth/WebConfig. */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Task>>> getTasks(
            @RequestParam Long eventId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long assigneeId,
            HttpSession session) {
        List<Task> tasks = taskService.getTasks(eventId, status, assigneeId, AuthSession.name(session));
        return ResponseEntity.ok(ApiResponse.ok("Tasks retrieved successfully", tasks));
    }

    @GetMapping("/assignees")
    public ResponseEntity<ApiResponse<List<AssigneeOption>>> getAssignees(
            @RequestParam(required = false) Long eventId,
            HttpSession session) {
        return ResponseEntity.ok(ApiResponse.ok("Assignees retrieved successfully",
                taskService.getAssignees(eventId, AuthSession.name(session))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Task>> getTask(@PathVariable("id") Long taskId, HttpSession session) {
        return ResponseEntity.ok(ApiResponse.ok("Task retrieved successfully",
                taskService.getTask(taskId, AuthSession.name(session))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Task>> createTask(@Valid @RequestBody CreateTaskRequest req,
                                                        HttpSession session) {
        Task created = taskService.createTask(req, AuthSession.userId(session), AuthSession.name(session));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Task created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Task>> updateTask(@PathVariable("id") Long taskId,
                                                        @Valid @RequestBody UpdateTaskRequest req,
                                                        HttpSession session) {
        return ResponseEntity.ok(ApiResponse.ok("Task updated successfully",
                taskService.updateTask(taskId, req, AuthSession.name(session))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable("id") Long taskId, HttpSession session) {
        taskService.deleteTask(taskId, AuthSession.name(session));
        return ResponseEntity.ok(ApiResponse.ok("Task with ID " + taskId + " deleted successfully."));
    }

    /** Task statistics for an event (for the event dashboard). */
    @GetMapping("/metrics/{eventId}")
    public ResponseEntity<ApiResponse<TaskMetrics>> getMetrics(@PathVariable Long eventId, HttpSession session) {
        return ResponseEntity.ok(ApiResponse.ok("Task metrics retrieved successfully",
                taskService.getMetrics(eventId, AuthSession.name(session))));
    }
}
