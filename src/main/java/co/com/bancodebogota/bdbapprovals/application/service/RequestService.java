package co.com.bancodebogota.bdbapprovals.application.service;

import co.com.bancodebogota.bdbapprovals.application.port.in.*;
import co.com.bancodebogota.bdbapprovals.application.port.out.*;
import co.com.bancodebogota.bdbapprovals.domain.exception.DomainException;
import co.com.bancodebogota.bdbapprovals.domain.model.*;
import co.com.bancodebogota.bdbapprovals.application.port.out.EmailPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RequestService implements
        CreateRequestUseCase, ApproveRequestUseCase, RejectRequestUseCase, CommentRequestUseCase,
        GetRequestQuery, ListRequestsQuery, GetHistoryQuery, GetInboxQuery {

    private final RequestRepository requestRepo;
    private final ActionRepository actionRepo;
    private final ClockPort clock;
    private final EmailPort emailAdapter;

    private static final Logger log = LoggerFactory.getLogger(RequestService.class);

    public RequestService(RequestRepository requestRepo, ActionRepository actionRepo,
                          ClockPort clock, EmailPort emailAdapter) {
        this.requestRepo = requestRepo;
        this.actionRepo = actionRepo;
        this.clock = clock;
        this.emailAdapter = emailAdapter;
    }

    @Override
    public ApprovalRequest create(String title, String description, String requesterUpn, String approverUpn, String type) {
        var now = clock.now();
        var req = new ApprovalRequest(UUID.randomUUID(), title, description, requesterUpn, approverUpn, type,
                RequestStatus.PENDING, now, now);
        var persisted = requestRepo.save(req);
        actionRepo.save(new ApprovalAction(UUID.randomUUID(), persisted.getId(), requesterUpn, ActionType.CREATE, "created", now));
        try {
            emailAdapter.sendRequestCreated(persisted);
        } catch (Exception e) {
            log.error("Failed to send 'created' notification for request {}. The request was saved. Error: {}",
                    persisted.getId(), e.getMessage(), e);
        }
        return persisted;
    }

    @Override
    public ApprovalRequest approve(UUID id, String actorUpn, String comment) {
        var req = requestRepo.findById(id).orElseThrow(() -> new DomainException("Request not found"));
        var action = req.approve(actorUpn, comment, clock.now());
        var updated = requestRepo.save(req);
        actionRepo.save(action);
        try {
            emailAdapter.sendRequestApproved(updated, comment);
        } catch (Exception e) {
            log.error("Failed to send 'approved' notification for request {}. The approval was saved. Error: {}",
                    id, e.getMessage(), e);
        }
        return updated;
    }

    @Override
    public ApprovalRequest reject(UUID id, String actorUpn, String comment) {
        var req = requestRepo.findById(id).orElseThrow(() -> new DomainException("Request not found"));
        var action = req.reject(actorUpn, comment, clock.now());
        var updated = requestRepo.save(req);
        actionRepo.save(action);
        try {
            emailAdapter.sendRequestRejected(updated, comment);
        } catch (Exception e) {
            log.error("Failed to send 'rejected' notification for request {}. The rejection was saved. Error: {}",
                    id, e.getMessage(), e);
        }
        return updated;
    }

    @Override
    public void comment(UUID id, String actorUpn, String comment) {
        var req = requestRepo.findById(id).orElseThrow(() -> new DomainException("Request not found"));
        var action = req.comment(actorUpn, comment, clock.now());
        actionRepo.save(action);
        requestRepo.save(req);
    }

    @Override
    @Transactional(readOnly = true)
    public ApprovalRequest get(UUID id) {
        return requestRepo.findById(id).orElseThrow(() -> new DomainException("Request not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalRequest> list(RequestStatus status, String type, String requesterUpn, String approverUpn,
                                      String createdFrom, String createdTo, int page, int size) {
        var from = createdFrom == null ? null : Instant.parse(createdFrom);
        var to = createdTo == null ? null : Instant.parse(createdTo);
        var f = new RequestRepository.SearchFilters(status, type, requesterUpn, approverUpn, from, to);
        return requestRepo.search(f, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalAction> history(UUID id) {
        // asegura existencia
        requestRepo.findById(id).orElseThrow(() -> new DomainException("Request not found"));
        return actionRepo.findByRequestId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalRequest> inbox(String approverUpn, int page, int size) {
        var f = new RequestRepository.SearchFilters(RequestStatus.PENDING, null, null, approverUpn, null, null);
        return requestRepo.search(f, page, size);
    }
}
