package co.com.bancodebogota.bdbapprovals.application.port.out;

import co.com.bancodebogota.bdbapprovals.domain.model.ApprovalAction;

import java.util.List;
import java.util.UUID;

public interface ActionRepository {
    void save(ApprovalAction action);
    List<ApprovalAction> findByRequestId(UUID requestId);
}
