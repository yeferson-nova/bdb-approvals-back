package co.com.bancodebogota.bdbapprovals.infrastructure.rest.dto;

import co.com.bancodebogota.bdbapprovals.domain.model.RequestStatus;

import java.time.Instant;
import java.util.UUID;

public record CreateRequestDto(String title, String description, String approverUpn, String type) { }

