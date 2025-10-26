package com.collabrix.auth.controller;

import com.collabrix.auth.dto.RoleRequest;
import com.collabrix.auth.dto.RoleResponse;
import com.collabrix.auth.entity.Role;
import com.collabrix.auth.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<RoleResponse> createRole(@RequestBody RoleRequest request) {
        Role role = roleService.createRole(request.getName(), false);
        log.info("Role created via API: {}", role.getName());
        return ResponseEntity.ok(toResponse(role));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        log.info("Role deleted via API: id={}", id);
        return ResponseEntity.ok("Role deleted successfully");
    }

    @GetMapping
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        List<RoleResponse> roles = roleService.getAllRoles()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(roles);
    }

    private RoleResponse toResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .systemDefined(role.isSystemDefined())
                .build();
    }
}
