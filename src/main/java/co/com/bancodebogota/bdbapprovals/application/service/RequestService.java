package co.com.bancodebogota.bdbapprovals.application.service;

import co.com.bancodebogota.bdbapprovals.application.port.in.*;
import co.com.bancodebogota.bdbapprovals.application.port.out.*;
import co.com.bancodebogota.bdbapprovals.domain.exception.DomainException;
import co.com.bancodebogota.bdbapprovals.domain.model.*;

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
    private final NotificationPort notifier;
    private final ClockPort clock;

    public RequestService(RequestRepository requestRepo, ActionRepository actionRepo,
                          NotificationPort notifier, ClockPort clock) {
        this.requestRepo = requestRepo;
        this.actionRepo = actionRepo;
        this.notifier = notifier;
        this.clock = clock;
    }

    @Override
    public ApprovalRequest create(String title, String description, String requesterUpn, String approverUpn, String type) {
        var now = clock.now();
        var req = new ApprovalRequest(UUID.randomUUID(), title, description, requesterUpn, approverUpn, type,
                RequestStatus.PENDING, now, now);
        var persisted = requestRepo.save(req);
        actionRepo.save(new ApprovalAction(UUID.randomUUID(), persisted.getId(), requesterUpn, ActionType.CREATE, "created", now));
        notifier.sendRequestCreated(persisted);
        return persisted;
    }

    @Override
    public ApprovalRequest approve(UUID id, String actorUpn, String comment) {
        var req = requestRepo.findById(id).orElseThrow(() -> new DomainException("Request not found"));
        var action = req.approve(actorUpn, comment, clock.now());
        var updated = requestRepo.save(req);
        actionRepo.save(action);
        notifier.sendRequestApproved(updated, comment);
        return updated;
    }

    @Override
    public ApprovalRequest reject(UUID id, String actorUpn, String comment) {
        var req = requestRepo.findById(id).orElseThrow(() -> new DomainException("Request not found"));
        var action = req.reject(actorUpn, comment, clock.now());
        var updated = requestRepo.save(req);
        actionRepo.save(action);
        notifier.sendRequestRejected(updated, comment);
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
