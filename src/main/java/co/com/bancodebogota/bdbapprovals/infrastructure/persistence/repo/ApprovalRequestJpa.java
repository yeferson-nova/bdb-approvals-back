package co.com.bancodebogota.bdbapprovals.infrastructure.persistence.repo;

import co.com.bancodebogota.bdbapprovals.infrastructure.persistence.entity.ApprovalRequestEntity;
import co.com.bancodebogota.bdbapprovals.infrastructure.persistence.entity.ApprovalActionEntity;
import co.com.bancodebogota.bdbapprovals.domain.model.RequestStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ApprovalRequestJpa extends JpaRepository<ApprovalRequestEntity, UUID> {
    List<ApprovalRequestEntity> findByStatusAndApproverUpn(RequestStatus status, String approverUpn, Pageable pageable);
    List<ApprovalRequestEntity> findByStatus(RequestStatus status, Pageable pageable);
    List<ApprovalRequestEntity> findByApproverUpn(String approverUpn, Pageable pageable);
    List<ApprovalRequestEntity> findByRequesterUpn(String requesterUpn, Pageable pageable);
    List<ApprovalRequestEntity> findByType(String type, Pageable pageable);
    List<ApprovalRequestEntity> findByCreatedAtBetween(Instant from, Instant to, Pageable pageable);
}