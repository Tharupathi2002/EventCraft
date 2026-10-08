package com.eventcraft.eventplanning.repository;

import com.eventcraft.eventplanning.entity.Event;
import com.eventcraft.eventplanning.entity.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for Event.
 * No SQL is written here - Spring generates the queries from the method
 * names. Once the SQL Server datasource is configured, these will run
 * against the real "events" table automatically.
 */
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByHostUsername(String hostUsername);

    List<Event> findByStatus(EventStatus status);

    List<Event> findByHostUsernameAndStatus(String hostUsername, EventStatus status);
}
