package com.eventcraft.taskschedule.controller;

import com.eventcraft.taskschedule.dto.ApiResponse;
import com.eventcraft.taskschedule.dto.CreateScheduleRequest;
import com.eventcraft.taskschedule.dto.UpdateScheduleRequest;
import com.eventcraft.taskschedule.model.Schedule;
import com.eventcraft.taskschedule.service.ScheduleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ScheduleController exposes REST APIs for managing event timeline and agenda in UC-17.
 */
@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @Autowired
    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    /**
     * GET /api/schedules
     * Retrieve chronological event timeline items.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Schedule>>> getAllSchedules(
            @RequestParam(required = false) Integer eventId) {
        List<Schedule> list = scheduleService.getAllSchedules(eventId);
        return ResponseEntity.ok(ApiResponse.ok("Schedule items retrieved successfully", list));
    }

    /**
     * GET /api/schedules/{id}
     * Retrieve single schedule item by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Schedule>> getScheduleById(@PathVariable("id") int scheduleId) {
        Schedule item = scheduleService.getScheduleById(scheduleId);
        return ResponseEntity.ok(ApiResponse.ok("Schedule item retrieved successfully", item));
    }

    /**
     * POST /api/schedules
     * Add a new schedule/timeline activity (validates time order and detects conflicts).
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Schedule>> createSchedule(@Valid @RequestBody CreateScheduleRequest req) {
        Schedule created = scheduleService.createSchedule(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Schedule activity created successfully", created));
    }

    /**
     * PUT /api/schedules/{id}
     * Update an activity (re-verifies time validity and detects conflicts).
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Schedule>> updateSchedule(
            @PathVariable("id") int scheduleId,
            @Valid @RequestBody UpdateScheduleRequest req) {
        Schedule updated = scheduleService.updateSchedule(scheduleId, req);
        return ResponseEntity.ok(ApiResponse.ok("Schedule activity updated successfully", updated));
    }

    /**
     * DELETE /api/schedules/{id}
     * Delete an activity from the event timeline.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(@PathVariable("id") int scheduleId) {
        scheduleService.deleteSchedule(scheduleId);
        return ResponseEntity.ok(ApiResponse.ok("Schedule item with ID " + scheduleId + " deleted successfully."));
    }
}
