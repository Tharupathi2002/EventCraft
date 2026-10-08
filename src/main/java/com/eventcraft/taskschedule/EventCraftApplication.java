package com.eventcraft.taskschedule;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

/**
 * Main entry point for the EventCraft Task and Schedule Management Module.
 * SE2030 Software Engineering - Module 5 (UC-17)
 */
@SpringBootApplication
public class EventCraftApplication {

    public static void main(String[] args) {
        // Check if a local .env file exists and load variables into System properties
        File envFile = new File(".env");
        if (envFile.exists()) {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            dotenv.entries().forEach(entry -> {
                if (System.getProperty(entry.getKey()) == null && System.getenv(entry.getKey()) == null) {
                    System.setProperty(entry.getKey(), entry.getValue());
                }
            });
            System.out.println(">>> [.env] Loaded environment variables from .env file successfully.");
        } else {
            System.out.println(">>> [.env] No .env file found. Falling back to system environment variables or defaults.");
        }

        SpringApplication.run(EventCraftApplication.class, args);
    }
}
