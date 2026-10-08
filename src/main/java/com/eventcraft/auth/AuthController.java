package com.eventcraft.auth;

import com.eventcraft.guestinvitation.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.Map;

/** Welcome screen, login, sign up and logout. */
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/")
    public String welcome(HttpSession session) {
        if (AuthSession.isLoggedIn(session)) {
            return "redirect:" + AuthSession.homeFor(AuthSession.role(session));
        }
        return "welcome";
    }

    // ---------------- login ----------------

    @GetMapping("/login")
    public String loginPage(@RequestParam(defaultValue = "ORGANIZER") String role,
                            HttpSession session, Model model) {
        if (AuthSession.isLoggedIn(session)) {
            return "redirect:" + AuthSession.homeFor(AuthSession.role(session));
        }
        model.addAttribute("role", cleanRole(role));
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        @RequestParam String role,
                        HttpServletRequest request, Model model) {
        try {
            User user = authService.authenticate(email, password, role);
            AuthSession.login(request, user);
            return "redirect:" + AuthSession.homeFor(user.getRole());
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("email", email);
            model.addAttribute("role", cleanRole(role));
            return "login";
        }
    }

    // ---------------- sign up ----------------

    @GetMapping("/signup")
    public String signupPage(@RequestParam(defaultValue = "ORGANIZER") String role,
                             HttpSession session, Model model) {
        if (AuthSession.isLoggedIn(session)) {
            return "redirect:" + AuthSession.homeFor(AuthSession.role(session));
        }
        model.addAttribute("role", cleanRole(role));
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@RequestParam String name,
                         @RequestParam String email,
                         @RequestParam String password,
                         @RequestParam String confirmPassword,
                         @RequestParam String role,
                         HttpServletRequest request, Model model) {
        try {
            User user = authService.signUp(name, email, password, confirmPassword, role);
            AuthSession.login(request, user);
            return "redirect:" + AuthSession.homeFor(user.getRole());
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            model.addAttribute("role", cleanRole(role));
            return "signup";
        }
    }

    // ---------------- logout / current user ----------------

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    /** Used by the event-planning page to auto-fill the host username. */
    @GetMapping("/api/auth/me")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> me(HttpSession session) {
        if (!AuthSession.isLoggedIn(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", AuthSession.userId(session));
        body.put("name", AuthSession.name(session));
        body.put("role", AuthSession.role(session));
        return ResponseEntity.ok(body);
    }

    private String cleanRole(String role) {
        return AuthSession.GUEST.equals(role) ? AuthSession.GUEST : AuthSession.ORGANIZER;
    }
}
