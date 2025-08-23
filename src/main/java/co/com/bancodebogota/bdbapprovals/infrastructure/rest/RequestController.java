package co.com.bancodebogota.bdbapprovals.infrastructure.rest;

import co.com.bancodebogota.bdbapprovals.application.port.in.*;
import co.com.bancodebogota.bdbapprovals.domain.model.*;
import co.com.bancodebogota.bdbapprovals.infrastructure.rest.dto.*;
import co.com.bancodebogota.bdbapprovals.infrastructure.rest.mapper.RequestRestMapper;
import co.com.bancodebogota.bdbapprovals.infrastructure.security.SecurityConfig;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class RequestController {

    private final CreateRequestUseCase createUC;
    private final ApproveRequestUseCase approveUC;
    private final RejectRequestUseCase rejectUC;
    private final CommentRequestUseCase commentUC;
    private final GetRequestQuery getQuery;
    private final ListRequestsQuery listQuery;
    private final GetHistoryQuery historyQuery;
    private final GetInboxQuery inboxQuery;
    private final RequestRestMapper mapper;

    public RequestController(CreateRequestUseCase createUC, ApproveRequestUseCase approveUC,
                             RejectRequestUseCase rejectUC, CommentRequestUseCase commentUC,
                             GetRequestQuery getQuery, ListRequestsQuery listQuery,
                             GetHistoryQuery historyQuery, GetInboxQuery inboxQuery,
                             RequestRestMapper mapper) {
        this.createUC = createUC; this.approveUC = approveUC; this.rejectUC = rejectUC; this.commentUC = commentUC;
        this.getQuery = getQuery; this.listQuery = listQuery; this.historyQuery = historyQuery; this.inboxQuery = inboxQuery;
        this.mapper = mapper;
    }

    @PostMapping("/requests")
    public ResponseEntity<RequestSummaryDto> create(@RequestBody CreateRequestDto dto, Authentication auth) {
        String requesterUpn = SecurityConfig.extractUpn((org.springframework.security.authentication.AbstractAuthenticationToken) auth);
        var saved = createUC.create(dto.title(), dto.description(), requesterUpn, dto.approverUpn(), dto.type());
        return ResponseEntity.ok(mapper.toSummary(saved));
    }

    @GetMapping("/requests/{id}")
    public ResponseEntity<RequestSummaryDto> get(@PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toSummary(getQuery.get(id)));
    }

    @GetMapping("/requests")
    public ResponseEntity<List<RequestSummaryDto>> list(
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String requesterUpn,
            @RequestParam(required = false) String approverUpn,
            @RequestParam(required = false) String createdFrom, // ISO-8601
            @RequestParam(required = false) String createdTo,   // ISO-8601
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var list = listQuery.list(status, type, requesterUpn, approverUpn, createdFrom, createdTo, page, size).stream()
                .map(mapper::toSummary).toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/inbox")
    public ResponseEntity<List<RequestSummaryDto>> inbox(Authentication auth,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "20") int size) {
        String approverUpn = SecurityConfig.extractUpn((org.springframework.security.authentication.AbstractAuthenticationToken) auth);
        var list = inboxQuery.inbox(approverUpn, page, size).stream().map(mapper::toSummary).toList();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/requests/{id}/approve")
    public ResponseEntity<RequestSummaryDto> approve(@PathVariable UUID id, @RequestBody(required = false) ActionDto dto,
                                                     Authentication auth) {
        String actor = SecurityConfig.extractUpn((org.springframework.security.authentication.AbstractAuthenticationToken) auth);
        var updated = approveUC.approve(id, actor, dto == null ? null : dto.comment());
        return ResponseEntity.ok(mapper.toSummary(updated));
    }

    @PostMapping("/requests/{id}/reject")
    public ResponseEntity<RequestSummaryDto> reject(@PathVariable UUID id, @RequestBody ActionDto dto,
                                                    Authentication auth) {
        String actor = SecurityConfig.extractUpn((org.springframework.security.authentication.AbstractAuthenticationToken) auth);
        var updated = rejectUC.reject(id, actor, dto == null ? null : dto.comment());
        return ResponseEntity.ok(mapper.toSummary(updated));
    }

    @PostMapping("/requests/{id}/comment")
    public ResponseEntity<Void> comment(@PathVariable UUID id, @RequestBody ActionDto dto, Authentication auth) {
        String actor = SecurityConfig.extractUpn((org.springframework.security.authentication.AbstractAuthenticationToken) auth);
        commentUC.comment(id, actor, dto.comment());
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/requests/{id}/history")
    public ResponseEntity<List<co.com.bancodebogota.bdbapprovals.domain.model.ApprovalAction>> history(@PathVariable UUID id) {
        return ResponseEntity.ok(historyQuery.history(id));
    }
}
