package com.eventcraft.auth;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor())
                .addPathPatterns(
                        "/organizer/**",
                        "/guest/**",
                        "/modules/event-planning/**",
                        "/api/events/**",
                        "/modules/task-schedule/**",
                        "/api/tasks/**",
                        "/api/schedules/**",
                        "/budgets",
                        "/budgets/**",
                        "/vendor-venue",
                        "/vendor-venue/**");
    }
}
