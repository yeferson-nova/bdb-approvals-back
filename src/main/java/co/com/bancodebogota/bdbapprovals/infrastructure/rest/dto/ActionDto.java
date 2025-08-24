package co.com.bancodebogota.bdbapprovals.infrastructure.rest.dto;

import co.com.bancodebogota.bdbapprovals.domain.model.ActionType;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;


public record ActionDto(
        UUID id,
        String authorUpn,
        ActionType type,
        String comment,
        Instant createdAt
) {}
