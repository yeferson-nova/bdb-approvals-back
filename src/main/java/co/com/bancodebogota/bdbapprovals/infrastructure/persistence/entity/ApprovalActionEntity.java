package co.com.bancodebogota.bdbapprovals.infrastructure.persistence.entity;

import co.com.bancodebogota.bdbapprovals.domain.model.ActionType;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "approval_actions")
public class ApprovalActionEntity {
    @Id
    private UUID id;
    private UUID requestId;
    private String actorUpn;
    @Enumerated(EnumType.STRING)
    private ActionType action;
    @Column(columnDefinition = "TEXT")
    private String comment;
    private Instant occurredAt;

    public UUID getId(){return id;} public void setId(UUID id){this.id=id;}
    public UUID getRequestId(){return requestId;} public void setRequestId(UUID v){this.requestId=v;}
    public String getActorUpn(){return actorUpn;} public void setActorUpn(String v){this.actorUpn=v;}
    public ActionType getAction(){return action;} public void setAction(ActionType a){this.action=a;}
    public String getComment(){return comment;} public void setComment(String v){this.comment=v;}
    public Instant getOccurredAt(){return occurredAt;} public void setOccurredAt(Instant v){this.occurredAt=v;}
}
