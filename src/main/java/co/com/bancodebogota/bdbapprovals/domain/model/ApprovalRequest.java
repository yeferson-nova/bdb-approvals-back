package co.com.bancodebogota.bdbapprovals.domain.model;

import co.com.bancodebogota.bdbapprovals.domain.exception.DomainException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class ApprovalRequest {

    private final UUID id;
    private final String title;
    private final String description;
    private final String requesterUpn;
    private final String approverUpn;
    private final String type;
    private RequestStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public ApprovalRequest(UUID id, String title, String description, String requesterUpn,
                           String approverUpn, String type, RequestStatus status,
                           Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.title = requireNotBlank(title, "title");
        this.description = description;
        this.requesterUpn = requireNotBlank(requesterUpn, "requesterUpn");
        this.approverUpn = requireNotBlank(approverUpn, "approverUpn");
        this.type = requireNotBlank(type, "type");
        this.status = Objects.requireNonNullElse(status, RequestStatus.PENDING);
        this.createdAt = Objects.requireNonNullElseGet(createdAt, Instant::now);
        this.updatedAt = Objects.requireNonNullElseGet(updatedAt, Instant::now);
    }

    private String requireNotBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new DomainException("Field '%s' is required".formatted(f));
        return v;
    }

    public ApprovalAction approve(String actorUpn, String comment, Instant now) {
        ensurePending();
        this.status = RequestStatus.APPROVED;
        this.updatedAt = now;
        return new ApprovalAction(UUID.randomUUID(), id, actorUpn, ActionType.APPROVE, comment, now);
    }

    public ApprovalAction reject(String actorUpn, String comment, Instant now) {
        ensurePending();
        if (comment == null || comment.isBlank())
            throw new DomainException("Reject requires a non-empty comment");
        this.status = RequestStatus.REJECTED;
        this.updatedAt = now;
        return new ApprovalAction(UUID.randomUUID(), id, actorUpn, ActionType.REJECT, comment, now);
    }

    public ApprovalAction comment(String actorUpn, String comment, Instant now) {
        if (comment == null || comment.isBlank())
            throw new DomainException("Comment cannot be empty");
        this.updatedAt = now;
        return new ApprovalAction(UUID.randomUUID(), id, actorUpn, ActionType.COMMENT, comment, now);
    }

    private void ensurePending() {
        if (this.status != RequestStatus.PENDING)
            throw new DomainException("Only PENDING requests can be approved/rejected");
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getRequesterUpn() { return requesterUpn; }
    public String getApproverUpn() { return approverUpn; }
    public String getType() { return type; }
    public RequestStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
