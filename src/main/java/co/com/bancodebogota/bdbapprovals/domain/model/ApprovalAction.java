package co.com.bancodebogota.bdbapprovals.domain.model;

import java.time.Instant;
import java.util.UUID;

public record ApprovalAction(
        UUID id,
        UUID requestId,
        String actorUpn,
        ActionType action,
        String comment,
        Instant occurredAt
) { }
