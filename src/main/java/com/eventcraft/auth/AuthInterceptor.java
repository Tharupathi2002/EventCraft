package com.eventcraft.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Guards the host area (/organizer/**, the event-planning page and /api/events/**)
 * and the guest area (/guest/**). See WebConfig for the registered paths.
 */
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String ctx = request.getContextPath();
        String path = request.getRequestURI().substring(ctx.length());
        boolean isApi = path.startsWith("/api/");

        HttpSession session = request.getSession(false);
        if (!AuthSession.isLoggedIn(session)) {
            if (isApi) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Please log in.");
            } else {
                response.sendRedirect(ctx + "/login");
            }
            return false;
        }

        String required = path.startsWith("/guest/") ? AuthSession.GUEST : AuthSession.ORGANIZER;
        String actual = AuthSession.role(session);
        if (!required.equals(actual)) {
            if (isApi) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Not allowed for your account type.");
            } else {
                response.sendRedirect(ctx + AuthSession.homeFor(actual));
            }
            return false;
        }
        return true;
    }
}
