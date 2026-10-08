package com.eventcraft.eventplanning.entity;

/**
 * Lifecycle status of an event.
 * DRAFT      - being edited by the host, not yet visible to guests.
 * PUBLISHED  - live and shareable.
 * CANCELLED  - the event was called off (soft delete alternative).
 */
public enum EventStatus {
    DRAFT,
    PUBLISHED,
    CANCELLED
}
