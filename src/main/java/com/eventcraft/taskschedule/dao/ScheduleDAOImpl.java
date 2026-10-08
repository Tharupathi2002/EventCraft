package com.eventcraft.taskschedule.dao;

import com.eventcraft.taskschedule.model.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ScheduleDAO Implementation using Spring JdbcTemplate.
 */
@Repository
public class ScheduleDAOImpl implements ScheduleDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ScheduleDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Schedule> scheduleRowMapper = new RowMapper<>() {
        @Override
        public Schedule mapRow(ResultSet rs, int rowNum) throws SQLException {
            Schedule schedule = new Schedule();
            schedule.setScheduleId(rs.getInt("schedule_id"));
            schedule.setEventId(rs.getInt("event_id"));
            schedule.setActivityName(rs.getString("activity_name"));
            schedule.setDescription(rs.getString("description"));

            Timestamp startTs = rs.getTimestamp("start_time");
            if (startTs != null) schedule.setStartTime(startTs.toLocalDateTime());

            Timestamp endTs = rs.getTimestamp("end_time");
            if (endTs != null) schedule.setEndTime(endTs.toLocalDateTime());

            schedule.setActivityType(rs.getString("activity_type"));
            schedule.setLocationOrStage(rs.getString("location_or_stage"));

            Timestamp createdTs = rs.getTimestamp("created_at");
            if (createdTs != null) schedule.setCreatedAt(createdTs.toLocalDateTime());

            schedule.setEventTitle(rs.getString("event_title"));

            return schedule;
        }
    };

    @Override
    public List<Schedule> findAll(Integer eventId) {
        StringBuilder sql = new StringBuilder("""
            SELECT 
                s.schedule_id, s.event_id, s.activity_name, s.description,
                s.start_time, s.end_time, s.activity_type, s.location_or_stage,
                s.created_at, e.event_title
            FROM Schedules s
            LEFT JOIN Events e ON s.event_id = e.event_id
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();
        if (eventId != null) {
            sql.append(" AND s.event_id = ?");
            params.add(eventId);
        }
        sql.append(" ORDER BY s.start_time ASC");

        return jdbcTemplate.query(sql.toString(), scheduleRowMapper, params.toArray());
    }

    @Override
    public Optional<Schedule> findById(int scheduleId) {
        String sql = """
            SELECT 
                s.schedule_id, s.event_id, s.activity_name, s.description,
                s.start_time, s.end_time, s.activity_type, s.location_or_stage,
                s.created_at, e.event_title
            FROM Schedules s
            LEFT JOIN Events e ON s.event_id = e.event_id
            WHERE s.schedule_id = ?
        """;

        List<Schedule> results = jdbcTemplate.query(sql, scheduleRowMapper, scheduleId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public Schedule create(Schedule schedule) {
        String sql = """
            INSERT INTO Schedules (event_id, activity_name, description, start_time, end_time, activity_type, location_or_stage, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, SYSUTCDATETIME())
        """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            java.sql.PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, schedule.getEventId());
            ps.setString(2, schedule.getActivityName());
            ps.setString(3, schedule.getDescription());
            ps.setTimestamp(4, Timestamp.valueOf(schedule.getStartTime()));
            ps.setTimestamp(5, Timestamp.valueOf(schedule.getEndTime()));
            ps.setString(6, schedule.getActivityType());
            ps.setString(7, schedule.getLocationOrStage());
            return ps;
        }, keyHolder);

        Number generatedKey = keyHolder.getKey();
        if (generatedKey != null) {
            schedule.setScheduleId(generatedKey.intValue());
        }

        return findById(schedule.getScheduleId()).orElse(schedule);
    }

    @Override
    public boolean update(Schedule schedule) {
        String sql = """
            UPDATE Schedules
            SET activity_name = ?, description = ?, start_time = ?, end_time = ?, activity_type = ?, location_or_stage = ?
            WHERE schedule_id = ?
        """;

        int rows = jdbcTemplate.update(sql,
                schedule.getActivityName(),
                schedule.getDescription(),
                Timestamp.valueOf(schedule.getStartTime()),
                Timestamp.valueOf(schedule.getEndTime()),
                schedule.getActivityType(),
                schedule.getLocationOrStage(),
                schedule.getScheduleId()
        );

        return rows > 0;
    }

    @Override
    public boolean delete(int scheduleId) {
        String sql = "DELETE FROM Schedules WHERE schedule_id = ?";
        return jdbcTemplate.update(sql, scheduleId) > 0;
    }

    @Override
    public int countConflicts(int eventId, LocalDateTime startTime, LocalDateTime endTime, Integer excludeScheduleId) {
        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(*) FROM Schedules
            WHERE event_id = ?
              AND (? < end_time AND ? > start_time)
        """);

        List<Object> params = new ArrayList<>();
        params.add(eventId);
        params.add(Timestamp.valueOf(startTime));
        params.add(Timestamp.valueOf(endTime));

        if (excludeScheduleId != null) {
            sql.append(" AND schedule_id != ?");
            params.add(excludeScheduleId);
        }

        Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
        return count != null ? count : 0;
    }

    @Override
    public boolean eventExists(int eventId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Events WHERE event_id = ?", Integer.class, eventId);
        return count != null && count > 0;
    }
}
