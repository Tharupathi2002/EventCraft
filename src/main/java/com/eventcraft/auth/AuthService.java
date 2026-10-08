package com.eventcraft.auth;

import com.eventcraft.guestinvitation.entity.User;
import com.eventcraft.guestinvitation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public User signUp(String name, String email, String password, String confirmPassword, String role) {
        String cleanName = name == null ? "" : name.trim();
        String cleanEmail = normalizeEmail(email);
        String cleanRole = normalizeRole(role);

        if (cleanName.isEmpty()) {
            throw new IllegalArgumentException("Please enter your name.");
        }
        if (cleanName.length() > 100) {
            throw new IllegalArgumentException("Name is too long (100 characters max).");
        }
        if (!cleanEmail.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        List<User> sameEmail = userRepository.findAllByEmailIgnoreCase(cleanEmail);
        if (sameEmail.stream().anyMatch(u -> u.getPassword() != null)) {
            throw new IllegalArgumentException("An account with this email already exists. Please log in.");
        }

        // A host may already have added this person to the guest directory (no password yet).
        // Signing up as a guest with the same email "claims" that record so past invitations carry over.
        Optional<User> unclaimedGuest = sameEmail.stream()
                .filter(u -> AuthSession.GUEST.equals(u.getRole()))
                .findFirst();
        if (AuthSession.GUEST.equals(cleanRole) && unclaimedGuest.isPresent()) {
            User guest = unclaimedGuest.get();
            guest.setName(cleanName);
            guest.setPassword(PasswordHasher.hash(password));
            return userRepository.save(guest);
        }
        if (!sameEmail.isEmpty()) {
            throw new IllegalArgumentException(
                    "This email is already on a guest list. Sign up as a Guest to claim it, or use a different email.");
        }

        // Events are listed under the host's name, so it has to be unique among hosts.
        if (AuthSession.ORGANIZER.equals(cleanRole)
                && userRepository.existsByNameIgnoreCaseAndRole(cleanName, AuthSession.ORGANIZER)) {
            throw new IllegalArgumentException("That host name is already taken. Please pick a different one.");
        }

        User user = new User();
        user.setName(cleanName);
        user.setEmail(cleanEmail);
        user.setPassword(PasswordHasher.hash(password));
        user.setRole(cleanRole);
        return userRepository.save(user);
    }

    public User authenticate(String email, String password, String role) {
        User user = userRepository.findAllByEmailIgnoreCase(normalizeEmail(email)).stream()
                .filter(u -> u.getPassword() != null && PasswordHasher.matches(password, u.getPassword()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        if (!normalizeRole(role).equals(user.getRole())) {
            String actual = AuthSession.GUEST.equals(user.getRole()) ? "Guest" : "Host";
            throw new IllegalArgumentException(
                    "This is a " + actual + " account. Switch the login type above and try again.");
        }
        return user;
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private String normalizeRole(String role) {
        return AuthSession.GUEST.equals(role) ? AuthSession.GUEST : AuthSession.ORGANIZER;
    }
}
