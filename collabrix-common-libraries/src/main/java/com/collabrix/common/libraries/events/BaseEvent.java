package com.collabrix.common.libraries.events;

import com.collabrix.common.libraries.dto.EventTypeEnum;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class BaseEvent {
    private UUID eventId;
    private EventTypeEnum eventType;
    private Long timestamp;
}
