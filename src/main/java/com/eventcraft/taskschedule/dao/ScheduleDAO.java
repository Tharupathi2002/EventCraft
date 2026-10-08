package com.eventcraft.taskschedule.dao;

import com.eventcraft.taskschedule.model.Schedule;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ScheduleDAO Interface.
 * Implements DAO pattern for the Schedules / Timeline table.
 */
public interface ScheduleDAO {
    List<Schedule> findAll(Integer eventId);
    Optional<Schedule> findById(int scheduleId);
    Schedule create(Schedule schedule);
    boolean update(Schedule schedule);
    boolean delete(int scheduleId);
    int countConflicts(int eventId, LocalDateTime startTime, LocalDateTime endTime, Integer excludeScheduleId);
    boolean eventExists(int eventId);
}
