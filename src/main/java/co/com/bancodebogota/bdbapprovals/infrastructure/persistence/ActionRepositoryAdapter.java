package co.com.bancodebogota.bdbapprovals.infrastructure.persistence;

import co.com.bancodebogota.bdbapprovals.application.port.out.ActionRepository;
import co.com.bancodebogota.bdbapprovals.domain.model.ApprovalAction;
import co.com.bancodebogota.bdbapprovals.infrastructure.persistence.mapper.ActionEntityMapper;
import co.com.bancodebogota.bdbapprovals.infrastructure.persistence.repo.ApprovalActionJpa;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class ActionRepositoryAdapter implements ActionRepository {
    private final ApprovalActionJpa jpa;
    private final ActionEntityMapper mapper;

    public ActionRepositoryAdapter(ApprovalActionJpa jpa, ActionEntityMapper mapper) {
        this.jpa = jpa; this.mapper = mapper;
    }

    @Override public void save(ApprovalAction action) { jpa.save(mapper.toEntity(action)); }

    @Override public List<ApprovalAction> findByRequestId(UUID requestId) {
        return jpa.findByRequestIdOrderByOccurredAtAsc(requestId).stream().map(mapper::toDomain).toList();
    }
}
