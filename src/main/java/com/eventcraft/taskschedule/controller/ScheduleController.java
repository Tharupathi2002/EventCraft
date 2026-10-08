package com.eventcraft.taskschedule.controller;

import com.eventcraft.auth.AuthSession;
import com.eventcraft.taskschedule.dto.ApiResponse;
import com.eventcraft.taskschedule.dto.CreateScheduleRequest;
import com.eventcraft.taskschedule.dto.UpdateScheduleRequest;
import com.eventcraft.taskschedule.entity.Schedule;
import com.eventcraft.taskschedule.service.ScheduleService;
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

/** REST API for the event schedule / timeline (UC-17). Host-only; see auth/WebConfig. */
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Schedule>>> getSchedules(@RequestParam Long eventId,
                                                                    HttpSession session) {
        return ResponseEntity.ok(ApiResponse.ok("Schedule items retrieved successfully",
                scheduleService.getSchedules(eventId, AuthSession.name(session))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Schedule>> getSchedule(@PathVariable("id") Long scheduleId,
                                                             HttpSession session) {
        return ResponseEntity.ok(ApiResponse.ok("Schedule item retrieved successfully",
                scheduleService.getSchedule(scheduleId, AuthSession.name(session))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Schedule>> createSchedule(@Valid @RequestBody CreateScheduleRequest req,
                                                                HttpSession session) {
        Schedule created = scheduleService.createSchedule(req, AuthSession.name(session));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Schedule activity created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Schedule>> updateSchedule(@PathVariable("id") Long scheduleId,
                                                                @Valid @RequestBody UpdateScheduleRequest req,
                                                                HttpSession session) {
        return ResponseEntity.ok(ApiResponse.ok("Schedule activity updated successfully",
                scheduleService.updateSchedule(scheduleId, req, AuthSession.name(session))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(@PathVariable("id") Long scheduleId,
                                                            HttpSession session) {
        scheduleService.deleteSchedule(scheduleId, AuthSession.name(session));
        return ResponseEntity.ok(ApiResponse.ok("Schedule item with ID " + scheduleId + " deleted successfully."));
    }
}
