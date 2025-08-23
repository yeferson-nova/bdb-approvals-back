package co.com.bancodebogota.bdbapprovals.infrastructure.rest.dto;

import co.com.bancodebogota.bdbapprovals.domain.model.RequestStatus;

import java.time.Instant;
import java.util.UUID;

public record RequestSummaryDto(UUID id, String title, String requesterUpn, String approverUpn, String type,
                                RequestStatus status, Instant createdAt, Instant updatedAt) { }