package com.collabrix.auth.service.interfaces;

import com.collabrix.auth.dto.RoleResponse;

public interface KeycloakRoleService {
    RoleResponse createRole(String roleName, String description);
    RoleResponse getRoleByName(String roleName);
    java.util.List<RoleResponse> getAllRoles();
    RoleResponse updateRole(String roleName, String newDescription);
    void deleteRole(String roleName);
}
