package co.com.bancodebogota.bdbapprovals.infrastructure.persistence.entity;

import co.com.bancodebogota.bdbapprovals.domain.model.RequestStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "approval_requests")
public class ApprovalRequestEntity {
    @Id
    private UUID id;
    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String requesterUpn;
    private String approverUpn;
    private String type;
    @Enumerated(EnumType.STRING)
    private RequestStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    // getters/setters
    // ...
    public UUID getId() { return id; } public void setId(UUID id){this.id=id;}
    public String getTitle(){return title;} public void setTitle(String t){this.title=t;}
    public String getDescription(){return description;} public void setDescription(String d){this.description=d;}
    public String getRequesterUpn(){return requesterUpn;} public void setRequesterUpn(String v){this.requesterUpn=v;}
    public String getApproverUpn(){return approverUpn;} public void setApproverUpn(String v){this.approverUpn=v;}
    public String getType(){return type;} public void setType(String v){this.type=v;}
    public RequestStatus getStatus(){return status;} public void setStatus(RequestStatus s){this.status=s;}
    public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant v){this.createdAt=v;}
    public Instant getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Instant v){this.updatedAt=v;}
}
