package com.eventcraft.config;

import com.eventcraft.models.Event;
import com.eventcraft.models.User;
import com.eventcraft.repositories.EventRepository;
import com.eventcraft.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public void run(String... args) {
        if (eventRepository.count() == 0) {
            Event event1 = new Event();
            event1.setEventName("Tech Meetup");
            event1.setTotalSeats(40);
            event1.setSeatsAvailable(40);
            eventRepository.save(event1);

            User guest = new User();
            guest.setName("Bob The Guest");
            guest.setRole("GUEST");
            userRepository.save(guest);
        }
    }
}