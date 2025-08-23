package co.com.bancodebogota.bdbapprovals.application.port.out;

import co.com.bancodebogota.bdbapprovals.domain.model.ApprovalRequest;
import co.com.bancodebogota.bdbapprovals.domain.model.RequestStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RequestRepository {
    ApprovalRequest save(ApprovalRequest request);
    Optional<ApprovalRequest> findById(UUID id);

    record SearchFilters(RequestStatus status, String type, String requesterUpn, String approverUpn,
                         Instant createdFrom, Instant createdTo) {}
    List<ApprovalRequest> search(SearchFilters filters, int page, int size);
}
