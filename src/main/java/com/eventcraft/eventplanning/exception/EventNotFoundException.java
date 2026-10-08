package com.eventcraft.eventplanning.exception;

/**
 * Thrown when an event with the given id does not exist.
 */
public class EventNotFoundException extends RuntimeException {

    public EventNotFoundException(Long eventId) {
        super("Event not found with id: " + eventId);
    }
}
