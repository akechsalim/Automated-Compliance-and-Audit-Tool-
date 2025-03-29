package com.complycore.compliance.compliance_tool.controller;

import com.complycore.compliance.compliance_tool.entity.User;
import com.complycore.compliance.compliance_tool.security.CustomUserDetails;
import com.complycore.compliance.compliance_tool.service.AuditLogService;
import com.complycore.compliance.compliance_tool.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final AuditLogService auditLogService;

    public UserController(UserService userService, AuditLogService auditLogService) {
        this.userService = userService;
        this.auditLogService = auditLogService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        auditLogService.log(createdUser.getUsername(), "USER_CREATED", "User created by admin");
        return ResponseEntity.ok(createdUser);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.findById(id)
                .map(user -> {
                    auditLogService.log(user.getUsername(), "USER_VIEWED", "User details retrieved");
                    return ResponseEntity.ok(user);
                })
                .orElseGet(() -> {
                    auditLogService.log(null, "USER_VIEW_FAILED", "User not found: " + id);
                    return ResponseEntity.notFound().build();
                });
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        User updatedUser = userService.updateUser(id, user);
        auditLogService.log(updatedUser.getUsername(), "USER_UPDATED", "User updated by admin");
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        auditLogService.log(null, "USER_DELETED", "User deleted: " + id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateMyProfile(@AuthenticationPrincipal CustomUserDetails principal,
                                                @Valid @RequestBody UpdateProfileRequest request) {
        User userUpdates = new User();
        userUpdates.setUsername(request.getUsername());
        userUpdates.setEmail(request.getEmail());
        User updatedUser = userService.updateUser(principal.getId(), userUpdates);
        auditLogService.log(principal.getUsername(), "PROFILE_UPDATED", "User updated own profile");
        return ResponseEntity.ok(updatedUser);
    }
}