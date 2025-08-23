package co.com.bancodebogota.bdbapprovals.infrastructure.persistence;

import co.com.bancodebogota.bdbapprovals.application.port.out.RequestRepository;
import co.com.bancodebogota.bdbapprovals.domain.model.ApprovalRequest;
import co.com.bancodebogota.bdbapprovals.domain.model.RequestStatus;
import co.com.bancodebogota.bdbapprovals.infrastructure.persistence.mapper.RequestEntityMapper;
import co.com.bancodebogota.bdbapprovals.infrastructure.persistence.repo.ApprovalRequestJpa;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class RequestRepositoryAdapter implements RequestRepository {

    private final ApprovalRequestJpa jpa;
    private final RequestEntityMapper mapper;

    public RequestRepositoryAdapter(ApprovalRequestJpa jpa, RequestEntityMapper mapper) {
        this.jpa = jpa; this.mapper = mapper;
    }

    @Override
    public ApprovalRequest save(ApprovalRequest request) {
        var saved = jpa.save(mapper.toEntity(request));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<ApprovalRequest> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<ApprovalRequest> search(SearchFilters f, int page, int size) {
        var pageable = PageRequest.of(page, size);
        List<co.com.bancodebogota.bdbapprovals.infrastructure.persistence.entity.ApprovalRequestEntity> list;

        if (f.status() != null && f.approverUpn() != null) {
            list = jpa.findByStatusAndApproverUpn(f.status(), f.approverUpn(), pageable);
        } else if (f.status() != null) {
            list = jpa.findByStatus(f.status(), pageable);
        } else if (f.approverUpn() != null) {
            list = jpa.findByApproverUpn(f.approverUpn(), pageable);
        } else if (f.requesterUpn() != null) {
            list = jpa.findByRequesterUpn(f.requesterUpn(), pageable);
        } else if (f.type() != null) {
            list = jpa.findByType(f.type(), pageable);
        } else if (f.createdFrom() != null && f.createdTo() != null) {
            list = jpa.findByCreatedAtBetween(f.createdFrom(), f.createdTo(), pageable);
        } else {
            list = jpa.findAll(pageable).getContent();
        }

        return list.stream().map(mapper::toDomain).toList();
    }
}
