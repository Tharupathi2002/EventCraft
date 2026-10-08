package com.eventcraft.taskschedule.service;

import com.eventcraft.eventplanning.entity.Event;
import com.eventcraft.eventplanning.repository.EventRepository;
import com.eventcraft.guestinvitation.entity.Invitation;
import com.eventcraft.guestinvitation.entity.User;
import com.eventcraft.guestinvitation.repository.InvitationRepository;
import com.eventcraft.guestinvitation.repository.UserRepository;
import com.eventcraft.taskschedule.dto.AssigneeOption;
import com.eventcraft.taskschedule.dto.CreateTaskRequest;
import com.eventcraft.taskschedule.dto.TaskMetrics;
import com.eventcraft.taskschedule.dto.UpdateTaskRequest;
import com.eventcraft.taskschedule.entity.Task;
import com.eventcraft.taskschedule.exception.ForbiddenException;
import com.eventcraft.taskschedule.exception.ResourceNotFoundException;
import com.eventcraft.taskschedule.exception.ValidationException;
import com.eventcraft.taskschedule.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Business rules for task management (UC-17). Every call is scoped to an event
 * owned by the logged-in host.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {

    private static final List<String> VALID_PRIORITIES = List.of("Low", "Medium", "High");
    private static final List<String> VALID_STATUSES = List.of("Pending", "In Progress", "Completed");

    private final TaskRepository taskRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;

    // ---------- reads ----------

    @Transactional(readOnly = true)
    public List<Task> getTasks(Long eventId, String status, Long assigneeId, String hostName) {
        Event event = requireOwnedEvent(eventId, hostName);
        boolean hasStatus = status != null && !status.isBlank();
        String st = hasStatus ? status.trim() : null;

        List<Task> tasks;
        if (hasStatus && assigneeId != null) {
            tasks = taskRepository.findByEventIdAndStatusAndAssignedToOrderByDeadlineAsc(eventId, st, assigneeId);
        } else if (hasStatus) {
            tasks = taskRepository.findByEventIdAndStatusOrderByDeadlineAsc(eventId, st);
        } else if (assigneeId != null) {
            tasks = taskRepository.findByEventIdAndAssignedToOrderByDeadlineAsc(eventId, assigneeId);
        } else {
            tasks = taskRepository.findByEventIdOrderByDeadlineAsc(eventId);
        }
        tasks.forEach(t -> decorate(t, event));
        return tasks;
    }

    @Transactional(readOnly = true)
    public Task getTask(Long taskId, String hostName) {
        Task task = findTask(taskId);
        Event event = requireOwnedEvent(task.getEventId(), hostName);
        return decorate(task, event);
    }

    @Transactional(readOnly = true)
    public TaskMetrics getMetrics(Long eventId, String hostName) {
        requireOwnedEvent(eventId, hostName);
        List<Task> tasks = taskRepository.findByEventIdOrderByDeadlineAsc(eventId);
        int completed = (int) tasks.stream().filter(t -> "Completed".equals(t.getStatus())).count();
        int inProgress = (int) tasks.stream().filter(t -> "In Progress".equals(t.getStatus())).count();
        int pending = (int) tasks.stream().filter(t -> "Pending".equals(t.getStatus())).count();
        double avg = tasks.stream().mapToInt(Task::getProgress).average().orElse(0);
        double rounded = Math.round(avg * 10.0) / 10.0;
        return new TaskMetrics(eventId, tasks.size(), completed, inProgress, pending, rounded);
    }

    /** Everyone who can be given a task (fallback). */
    @Transactional(readOnly = true)
    public List<AssigneeOption> getAssignees() {
        return getAssignees(null, null);
    }

    /**
     * Users who can be assigned a task. If eventId is provided, only the event host
     * and invited guests for this event are returned.
     */
    @Transactional(readOnly = true)
    public List<AssigneeOption> getAssignees(Long eventId, String hostName) {
        if (eventId == null) {
            return userRepository.findAll().stream()
                    .map(u -> new AssigneeOption(u.getId(), u.getName(), u.getRole()))
                    .toList();
        }

        Event event = requireOwnedEvent(eventId, hostName);
        List<AssigneeOption> assignees = new ArrayList<>();
        Set<Long> seenIds = new HashSet<>();

        // 1. Add the host
        userRepository.findAll().stream()
                .filter(u -> u.getName() != null && (u.getName().equalsIgnoreCase(event.getHostUsername())
                        || (hostName != null && u.getName().equalsIgnoreCase(hostName))))
                .findFirst()
                .ifPresent(hostUser -> {
                    assignees.add(new AssigneeOption(hostUser.getId(), hostUser.getName(), hostUser.getRole()));
                    seenIds.add(hostUser.getId());
                });

        // 2. Add invited guests for this event
        List<Invitation> invitations = invitationRepository.findByEvent_EventId(eventId);
        for (Invitation inv : invitations) {
            User guest = inv.getGuest();
            if (guest != null && guest.getId() != null && seenIds.add(guest.getId())) {
                assignees.add(new AssigneeOption(guest.getId(), guest.getName(), guest.getRole()));
            }
        }

        return assignees;
    }

    // ---------- writes ----------

    public Task createTask(CreateTaskRequest req, Long creatorId, String hostName) {
        Event event = requireOwnedEvent(req.getEventId(), hostName);

        if (req.getAssignedTo() != null && !userRepository.existsById(req.getAssignedTo())) {
            throw new ValidationException("Invalid assignee: that user does not exist.");
        }

        String priority = req.getPriority() != null ? req.getPriority().trim() : "Medium";
        if (!VALID_PRIORITIES.contains(priority)) {
            throw new ValidationException("Invalid Priority. Allowed values: Low, Medium, High.");
        }

        if (req.getDeadline() == null) {
            throw new ValidationException("Invalid Deadline: Deadline time must be provided.");
        }

        Task task = new Task();
        task.setEventId(req.getEventId());
        task.setTitle(req.getTitle().trim());
        task.setDescription(req.getDescription());
        task.setDeadline(req.getDeadline());
        task.setPriority(priority);
        task.setStatus("Pending");
        task.setProgress(0);
        task.setAssignedTo(req.getAssignedTo());
        task.setCreatedBy(creatorId);

        return decorate(taskRepository.save(task), event);
    }

    public Task updateTask(Long taskId, UpdateTaskRequest req, String hostName) {
        Task existing = findTask(taskId);
        Event event = requireOwnedEvent(existing.getEventId(), hostName);

        if (req.getTitle() != null && !req.getTitle().trim().isEmpty()) {
            existing.setTitle(req.getTitle().trim());
        }
        if (req.getDescription() != null) {
            existing.setDescription(req.getDescription());
        }
        if (req.getDeadline() != null) {
            existing.setDeadline(req.getDeadline());
        }
        if (req.getPriority() != null) {
            String p = req.getPriority().trim();
            if (!VALID_PRIORITIES.contains(p)) {
                throw new ValidationException("Invalid Priority. Allowed: Low, Medium, High.");
            }
            existing.setPriority(p);
        }
        if (req.getAssignedTo() != null) {
            if (!userRepository.existsById(req.getAssignedTo())) {
                throw new ValidationException("Invalid assignee: that user does not exist.");
            }
            existing.setAssignedTo(req.getAssignedTo());
        }

        // Progress drives status automatically; an explicit status is applied after.
        if (req.getProgress() != null) {
            int prog = req.getProgress();
            if (prog < 0 || prog > 100) {
                throw new ValidationException("Progress must be between 0 and 100.");
            }
            existing.setProgress(prog);
            if (prog == 100) {
                existing.setStatus("Completed");
            } else if (prog > 0 && "Pending".equalsIgnoreCase(existing.getStatus())) {
                existing.setStatus("In Progress");
            }
        }
        if (req.getStatus() != null) {
            String s = req.getStatus().trim();
            if (!VALID_STATUSES.contains(s)) {
                throw new ValidationException("Invalid Status. Allowed: Pending, In Progress, Completed.");
            }
            existing.setStatus(s);
            if ("Completed".equalsIgnoreCase(s) && existing.getProgress() < 100) {
                existing.setProgress(100);
            }
        }

        return decorate(taskRepository.save(existing), event);
    }

    public void deleteTask(Long taskId, String hostName) {
        Task task = findTask(taskId);
        requireOwnedEvent(task.getEventId(), hostName);
        taskRepository.delete(task);
    }

    /** Called by event-planning when an event is deleted. */
    public void deleteByEventId(Long eventId) {
        taskRepository.deleteByEventId(eventId);
    }

    // ---------- helpers ----------

    private Task findTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task with ID " + taskId + " not found."));
    }

    private Event requireOwnedEvent(Long eventId, String hostName) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event with ID " + eventId + " not found."));
        if (hostName == null || !hostName.equals(event.getHostUsername())) {
            throw new ForbiddenException("You can only manage tasks for your own events.");
        }
        return event;
    }

    private Task decorate(Task task, Event event) {
        task.setEventTitle(event.getEventName());
        if (task.getAssignedTo() != null) {
            task.setAssigneeName(userRepository.findById(task.getAssignedTo()).map(User::getName).orElse(null));
        }
        if (task.getCreatedBy() != null) {
            task.setCreatorName(userRepository.findById(task.getCreatedBy()).map(User::getName).orElse(null));
        }
        return task;
    }
}
