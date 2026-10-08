package com.eventcraft.guestinvitation.repository;
import com.eventcraft.guestinvitation.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByRole(String role);
    List<User> findAllByEmailIgnoreCase(String email);
    boolean existsByNameIgnoreCaseAndRole(String name, String role);
}