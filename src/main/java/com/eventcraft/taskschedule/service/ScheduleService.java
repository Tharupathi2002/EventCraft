package com.eventcraft.taskschedule.service;

import com.eventcraft.taskschedule.dao.ScheduleDAO;
import com.eventcraft.taskschedule.dto.CreateScheduleRequest;
import com.eventcraft.taskschedule.dto.UpdateScheduleRequest;
import com.eventcraft.taskschedule.exception.ResourceNotFoundException;
import com.eventcraft.taskschedule.exception.ScheduleConflictException;
import com.eventcraft.taskschedule.exception.ValidationException;
import com.eventcraft.taskschedule.model.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * ScheduleService handles business rules and conflict detection for Schedule/Timeline.
 */
@Service
public class ScheduleService {

    private final ScheduleDAO scheduleDAO;
    private static final List<String> VALID_TYPES = Arrays.asList(
            "Milestone", "Activity", "Session", "Keynote", "Break"
    );

    @Autowired
    public ScheduleService(ScheduleDAO scheduleDAO) {
        this.scheduleDAO = scheduleDAO;
    }

    public List<Schedule> getAllSchedules(Integer eventId) {
        return scheduleDAO.findAll(eventId);
    }

    public Schedule getScheduleById(int scheduleId) {
        return scheduleDAO.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule item with ID " + scheduleId + " not found."));
    }

    public Schedule createSchedule(CreateScheduleRequest req) {
        // 1. Verify Event exists
        if (!scheduleDAO.eventExists(req.getEventId())) {
            throw new ValidationException("Invalid Event ID: Event does not exist.");
        }

        // 2. Validate Time Order (End time must be after Start time)
        if (req.getStartTime() == null || req.getEndTime() == null) {
            throw new ValidationException("Both start time and end time are required.");
        }
        if (!req.getEndTime().isAfter(req.getStartTime())) {
            throw new ValidationException("Invalid Time Range: End time must be strictly after start time.");
        }

        // 3. Validate Activity Type
        String type = req.getActivityType() != null ? req.getActivityType().trim() : "Activity";
        if (!VALID_TYPES.contains(type)) {
            throw new ValidationException("Invalid Activity Type. Allowed: " + VALID_TYPES);
        }

        // 4. Check for Schedule Conflicts (Overlapping activities in the same event)
        int conflicts = scheduleDAO.countConflicts(req.getEventId(), req.getStartTime(), req.getEndTime(), null);
        if (conflicts > 0) {
            throw new ScheduleConflictException(
                "Scheduling Conflict Detected: Another activity is already scheduled during the requested time window."
            );
        }

        Schedule schedule = new Schedule();
        schedule.setEventId(req.getEventId());
        schedule.setActivityName(req.getActivityName().trim());
        schedule.setDescription(req.getDescription());
        schedule.setStartTime(req.getStartTime());
        schedule.setEndTime(req.getEndTime());
        schedule.setActivityType(type);
        schedule.setLocationOrStage(req.getLocationOrStage());

        return scheduleDAO.create(schedule);
    }

    public Schedule updateSchedule(int scheduleId, UpdateScheduleRequest req) {
        Schedule existing = getScheduleById(scheduleId);

        if (req.getActivityName() != null && !req.getActivityName().trim().isEmpty()) {
            existing.setActivityName(req.getActivityName().trim());
        }

        if (req.getDescription() != null) {
            existing.setDescription(req.getDescription());
        }

        LocalDateTime newStart = req.getStartTime() != null ? req.getStartTime() : existing.getStartTime();
        LocalDateTime newEnd = req.getEndTime() != null ? req.getEndTime() : existing.getEndTime();

        if (!newEnd.isAfter(newStart)) {
            throw new ValidationException("Invalid Time Range: End time must be strictly after start time.");
        }

        // Check for conflicts excluding this current schedule item
        int conflicts = scheduleDAO.countConflicts(existing.getEventId(), newStart, newEnd, scheduleId);
        if (conflicts > 0) {
            throw new ScheduleConflictException(
                "Scheduling Conflict Detected: Rescheduled time window overlaps with an existing event activity."
            );
        }

        existing.setStartTime(newStart);
        existing.setEndTime(newEnd);

        if (req.getActivityType() != null) {
            String type = req.getActivityType().trim();
            if (!VALID_TYPES.contains(type)) {
                throw new ValidationException("Invalid Activity Type. Allowed: " + VALID_TYPES);
            }
            existing.setActivityType(type);
        }

        if (req.getLocationOrStage() != null) {
            existing.setLocationOrStage(req.getLocationOrStage());
        }

        boolean updated = scheduleDAO.update(existing);
        if (!updated) {
            throw new RuntimeException("Failed to update schedule item with ID " + scheduleId);
        }

        return getScheduleById(scheduleId);
    }

    public void deleteSchedule(int scheduleId) {
        getScheduleById(scheduleId);
        scheduleDAO.delete(scheduleId);
    }
}
