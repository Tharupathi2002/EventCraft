package com.eventcraft.taskschedule.dto;

/** A user who can be assigned a task. Deliberately has no email or password. */
public record AssigneeOption(Long id, String name, String role) {
}
