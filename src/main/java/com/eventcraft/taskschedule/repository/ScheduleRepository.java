package com.eventcraft.taskschedule.repository;

import com.eventcraft.taskschedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByEventIdOrderByStartTimeAsc(Long eventId);

    /** Items in the same event whose time window overlaps [start, end). Pass excludeId = -1 for none. */
    @Query("""
            SELECT COUNT(s) FROM Schedule s
            WHERE s.eventId = :eventId
              AND :rangeStart < s.endTime AND :rangeEnd > s.startTime
              AND s.scheduleId <> :excludeId
            """)
    long countConflicts(@Param("eventId") Long eventId,
                        @Param("rangeStart") LocalDateTime start,
                        @Param("rangeEnd") LocalDateTime end,
                        @Param("excludeId") Long excludeId);

    void deleteByEventId(Long eventId);
}
