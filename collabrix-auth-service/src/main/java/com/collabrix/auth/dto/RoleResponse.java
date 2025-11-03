package com.collabrix.auth.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {
    private String id;
    private String name;
    private String description;
    private Boolean composite;
    private Boolean clientRole;
}