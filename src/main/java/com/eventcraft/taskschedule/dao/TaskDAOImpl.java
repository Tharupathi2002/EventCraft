package com.eventcraft.taskschedule.dao;

import com.eventcraft.taskschedule.model.Task;
import com.eventcraft.taskschedule.model.TaskMetrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * TaskDAO Implementation using Spring JdbcTemplate.
 * Executes exact SQL Server queries for CRUD and metric retrieval.
 */
@Repository
public class TaskDAOImpl implements TaskDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public TaskDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Task> taskRowMapper = new RowMapper<>() {
        @Override
        public Task mapRow(ResultSet rs, int rowNum) throws SQLException {
            Task task = new Task();
            task.setTaskId(rs.getInt("task_id"));
            task.setEventId(rs.getInt("event_id"));
            task.setTitle(rs.getString("title"));
            task.setDescription(rs.getString("description"));

            Timestamp deadlineTs = rs.getTimestamp("deadline");
            if (deadlineTs != null) task.setDeadline(deadlineTs.toLocalDateTime());

            task.setPriority(rs.getString("priority"));
            task.setStatus(rs.getString("status"));
            task.setProgress(rs.getInt("progress"));

            int assignedTo = rs.getInt("assigned_to");
            if (!rs.wasNull()) task.setAssignedTo(assignedTo);

            task.setCreatedBy(rs.getInt("created_by"));

            Timestamp createdAtTs = rs.getTimestamp("created_at");
            if (createdAtTs != null) task.setCreatedAt(createdAtTs.toLocalDateTime());

            Timestamp updatedAtTs = rs.getTimestamp("updated_at");
            if (updatedAtTs != null) task.setUpdatedAt(updatedAtTs.toLocalDateTime());

            // Joined columns
            task.setAssigneeName(rs.getString("assignee_name"));
            task.setCreatorName(rs.getString("creator_name"));
            task.setEventTitle(rs.getString("event_title"));

            return task;
        }
    };

    @Override
    public List<Task> findAll(Integer eventId, String status, Integer assigneeId) {
        StringBuilder sql = new StringBuilder("""
            SELECT 
                t.task_id, t.event_id, t.title, t.description, t.deadline,
                t.priority, t.status, t.progress, t.assigned_to, t.created_by,
                t.created_at, t.updated_at,
                u_assignee.full_name AS assignee_name,
                u_creator.full_name AS creator_name,
                e.event_title
            FROM Tasks t
            LEFT JOIN Users u_assignee ON t.assigned_to = u_assignee.user_id
            LEFT JOIN Users u_creator ON t.created_by = u_creator.user_id
            LEFT JOIN Events e ON t.event_id = e.event_id
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();
        if (eventId != null) {
            sql.append(" AND t.event_id = ?");
            params.add(eventId);
        }
        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND t.status = ?");
            params.add(status.trim());
        }
        if (assigneeId != null) {
            sql.append(" AND t.assigned_to = ?");
            params.add(assigneeId);
        }
        sql.append(" ORDER BY t.deadline ASC");

        return jdbcTemplate.query(sql.toString(), taskRowMapper, params.toArray());
    }

    @Override
    public Optional<Task> findById(int taskId) {
        String sql = """
            SELECT 
                t.task_id, t.event_id, t.title, t.description, t.deadline,
                t.priority, t.status, t.progress, t.assigned_to, t.created_by,
                t.created_at, t.updated_at,
                u_assignee.full_name AS assignee_name,
                u_creator.full_name AS creator_name,
                e.event_title
            FROM Tasks t
            LEFT JOIN Users u_assignee ON t.assigned_to = u_assignee.user_id
            LEFT JOIN Users u_creator ON t.created_by = u_creator.user_id
            LEFT JOIN Events e ON t.event_id = e.event_id
            WHERE t.task_id = ?
        """;

        List<Task> results = jdbcTemplate.query(sql, taskRowMapper, taskId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public Task create(Task task) {
        String sql = """
            INSERT INTO Tasks (event_id, title, description, deadline, priority, status, progress, assigned_to, created_by, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, SYSUTCDATETIME(), SYSUTCDATETIME())
        """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            java.sql.PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, task.getEventId());
            ps.setString(2, task.getTitle());
            ps.setString(3, task.getDescription());
            ps.setTimestamp(4, Timestamp.valueOf(task.getDeadline()));
            ps.setString(5, task.getPriority());
            ps.setString(6, task.getStatus());
            ps.setInt(7, task.getProgress());
            if (task.getAssignedTo() != null) {
                ps.setInt(8, task.getAssignedTo());
            } else {
                ps.setNull(8, java.sql.Types.INTEGER);
            }
            ps.setInt(9, task.getCreatedBy());
            return ps;
        }, keyHolder);

        Number generatedKey = keyHolder.getKey();
        if (generatedKey != null) {
            task.setTaskId(generatedKey.intValue());
        }

        return findById(task.getTaskId()).orElse(task);
    }

    @Override
    public boolean update(Task task) {
        String sql = """
            UPDATE Tasks
            SET title = ?, description = ?, deadline = ?, priority = ?, status = ?, progress = ?, assigned_to = ?, updated_at = SYSUTCDATETIME()
            WHERE task_id = ?
        """;

        int rows = jdbcTemplate.update(sql,
                task.getTitle(),
                task.getDescription(),
                Timestamp.valueOf(task.getDeadline()),
                task.getPriority(),
                task.getStatus(),
                task.getProgress(),
                task.getAssignedTo(),
                task.getTaskId()
        );

        return rows > 0;
    }

    @Override
    public boolean delete(int taskId) {
        String sql = "DELETE FROM Tasks WHERE task_id = ?";
        return jdbcTemplate.update(sql, taskId) > 0;
    }

    @Override
    public TaskMetrics getMetrics(int eventId) {
        String sql = """
            SELECT 
                COUNT(*) AS total_tasks,
                SUM(CASE WHEN status = 'Completed' THEN 1 ELSE 0 END) AS completed_tasks,
                SUM(CASE WHEN status = 'In Progress' THEN 1 ELSE 0 END) AS in_progress_tasks,
                SUM(CASE WHEN status = 'Pending' THEN 1 ELSE 0 END) AS pending_tasks,
                ISNULL(ROUND(AVG(CAST(progress AS FLOAT)), 1), 0) AS avg_progress
            FROM Tasks
            WHERE event_id = ?
        """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new TaskMetrics(
                eventId,
                rs.getInt("total_tasks"),
                rs.getInt("completed_tasks"),
                rs.getInt("in_progress_tasks"),
                rs.getInt("pending_tasks"),
                rs.getDouble("avg_progress")
        ), eventId);
    }

    @Override
    public boolean userExists(int userId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Users WHERE user_id = ?", Integer.class, userId);
        return count != null && count > 0;
    }

    @Override
    public boolean eventExists(int eventId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Events WHERE event_id = ?", Integer.class, eventId);
        return count != null && count > 0;
    }
}
