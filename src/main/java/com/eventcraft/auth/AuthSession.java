package com.eventcraft.auth;

import com.eventcraft.guestinvitation.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/** Small helper for reading/writing the logged-in user in the HTTP session. */
public final class AuthSession {

    public static final String ORGANIZER = "ORGANIZER";
    public static final String GUEST = "GUEST";

    private static final String USER_ID = "authUserId";
    private static final String USER_NAME = "authUserName";
    private static final String USER_ROLE = "authUserRole";

    private AuthSession() {
    }

    public static void login(HttpServletRequest request, User user) {
        HttpSession old = request.getSession(false);
        if (old != null) {
            old.invalidate(); // start a fresh session on every login
        }
        HttpSession session = request.getSession(true);
        session.setAttribute(USER_ID, user.getId());
        session.setAttribute(USER_NAME, user.getName());
        session.setAttribute(USER_ROLE, user.getRole());
    }

    public static boolean isLoggedIn(HttpSession session) {
        return session != null && session.getAttribute(USER_ID) != null;
    }

    public static Long userId(HttpSession session) {
        return session == null ? null : (Long) session.getAttribute(USER_ID);
    }

    public static String name(HttpSession session) {
        return session == null ? null : (String) session.getAttribute(USER_NAME);
    }

    public static String role(HttpSession session) {
        return session == null ? null : (String) session.getAttribute(USER_ROLE);
    }

    /** Where each role lands after logging in. */
    public static String homeFor(String role) {
        return GUEST.equals(role) ? "/guest/dashboard" : "/organizer/events";
    }
}
