package com.eventcraft.taskschedule.service;

import com.eventcraft.eventplanning.entity.Event;
import com.eventcraft.eventplanning.repository.EventRepository;
import com.eventcraft.taskschedule.dto.CreateScheduleRequest;
import com.eventcraft.taskschedule.dto.UpdateScheduleRequest;
import com.eventcraft.taskschedule.entity.Schedule;
import com.eventcraft.taskschedule.exception.ForbiddenException;
import com.eventcraft.taskschedule.exception.ResourceNotFoundException;
import com.eventcraft.taskschedule.exception.ScheduleConflictException;
import com.eventcraft.taskschedule.exception.ValidationException;
import com.eventcraft.taskschedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Business rules and conflict detection for the event schedule / timeline (UC-17).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleService {

    private static final List<String> VALID_TYPES = List.of("Milestone", "Activity", "Session", "Keynote", "Break");

    private final ScheduleRepository scheduleRepository;
    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public List<Schedule> getSchedules(Long eventId, String hostName) {
        Event event = requireOwnedEvent(eventId, hostName);
        List<Schedule> items = scheduleRepository.findByEventIdOrderByStartTimeAsc(eventId);
        items.forEach(s -> s.setEventTitle(event.getEventName()));
        return items;
    }

    @Transactional(readOnly = true)
    public Schedule getSchedule(Long scheduleId, String hostName) {
        Schedule item = findSchedule(scheduleId);
        Event event = requireOwnedEvent(item.getEventId(), hostName);
        item.setEventTitle(event.getEventName());
        return item;
    }

    public Schedule createSchedule(CreateScheduleRequest req, String hostName) {
        Event event = requireOwnedEvent(req.getEventId(), hostName);

        if (req.getStartTime() == null || req.getEndTime() == null) {
            throw new ValidationException("Both start time and end time are required.");
        }
        if (!req.getEndTime().isAfter(req.getStartTime())) {
            throw new ValidationException("Invalid Time Range: End time must be strictly after start time.");
        }

        String type = req.getActivityType() != null ? req.getActivityType().trim() : "Activity";
        if (!VALID_TYPES.contains(type)) {
            throw new ValidationException("Invalid Activity Type. Allowed: " + VALID_TYPES);
        }

        if (scheduleRepository.countConflicts(req.getEventId(), req.getStartTime(), req.getEndTime(), -1L) > 0) {
            throw new ScheduleConflictException(
                    "Scheduling Conflict Detected: Another activity is already scheduled during the requested time window.");
        }

        Schedule schedule = new Schedule();
        schedule.setEventId(req.getEventId());
        schedule.setActivityName(req.getActivityName().trim());
        schedule.setDescription(req.getDescription());
        schedule.setStartTime(req.getStartTime());
        schedule.setEndTime(req.getEndTime());
        schedule.setActivityType(type);
        schedule.setLocationOrStage(req.getLocationOrStage());

        Schedule saved = scheduleRepository.save(schedule);
        saved.setEventTitle(event.getEventName());
        return saved;
    }

    public Schedule updateSchedule(Long scheduleId, UpdateScheduleRequest req, String hostName) {
        Schedule existing = findSchedule(scheduleId);
        Event event = requireOwnedEvent(existing.getEventId(), hostName);

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
        if (scheduleRepository.countConflicts(existing.getEventId(), newStart, newEnd, scheduleId) > 0) {
            throw new ScheduleConflictException(
                    "Scheduling Conflict Detected: Rescheduled time window overlaps with an existing event activity.");
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

        Schedule saved = scheduleRepository.save(existing);
        saved.setEventTitle(event.getEventName());
        return saved;
    }

    public void deleteSchedule(Long scheduleId, String hostName) {
        Schedule item = findSchedule(scheduleId);
        requireOwnedEvent(item.getEventId(), hostName);
        scheduleRepository.delete(item);
    }

    /** Called by event-planning when an event is deleted. */
    public void deleteByEventId(Long eventId) {
        scheduleRepository.deleteByEventId(eventId);
    }

    private Schedule findSchedule(Long scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule item with ID " + scheduleId + " not found."));
    }

    private Event requireOwnedEvent(Long eventId, String hostName) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event with ID " + eventId + " not found."));
        if (hostName == null || !hostName.equals(event.getHostUsername())) {
            throw new ForbiddenException("You can only manage the schedule of your own events.");
        }
        return event;
    }
}
