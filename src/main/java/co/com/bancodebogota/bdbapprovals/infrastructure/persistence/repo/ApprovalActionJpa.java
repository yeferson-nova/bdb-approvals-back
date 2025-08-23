package co.com.bancodebogota.bdbapprovals.infrastructure.persistence.repo;

import co.com.bancodebogota.bdbapprovals.infrastructure.persistence.entity.ApprovalActionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApprovalActionJpa extends JpaRepository<ApprovalActionEntity, UUID> {
    List<ApprovalActionEntity> findByRequestIdOrderByOccurredAtAsc(UUID requestId);
}
