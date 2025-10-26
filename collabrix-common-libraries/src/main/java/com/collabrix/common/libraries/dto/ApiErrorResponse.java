package com.collabrix.common.libraries.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class ApiErrorResponse {
    private final Instant timestamp;
    private final String message;
    private final String serviceName;
}
