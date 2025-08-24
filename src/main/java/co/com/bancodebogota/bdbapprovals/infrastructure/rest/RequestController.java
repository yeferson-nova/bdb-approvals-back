package co.com.bancodebogota.bdbapprovals.infrastructure.rest;

import co.com.bancodebogota.bdbapprovals.application.port.in.*;
import co.com.bancodebogota.bdbapprovals.domain.exception.DomainException;
import co.com.bancodebogota.bdbapprovals.domain.model.ApprovalAction;
import co.com.bancodebogota.bdbapprovals.domain.model.ApprovalRequest;
import co.com.bancodebogota.bdbapprovals.domain.model.RequestStatus;
import co.com.bancodebogota.bdbapprovals.infrastructure.rest.dto.CreateRequestDto;
import co.com.bancodebogota.bdbapprovals.infrastructure.rest.dto.RequestDetailDto;
import co.com.bancodebogota.bdbapprovals.infrastructure.rest.dto.RequestSummaryDto;
import co.com.bancodebogota.bdbapprovals.infrastructure.rest.mapper.RequestRestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.UUID;

@RestController
@RequestMapping("/api/requests")
public class RequestController {

    private final CreateRequestUseCase createUC;
    private final ApproveRequestUseCase approveUC;
    private final RejectRequestUseCase rejectUC;
    private final GetRequestQuery getQuery;
    private final ListRequestsQuery listQuery;
    private final GetHistoryQuery historyQuery;
    private final RequestRestMapper mapper;

    @Autowired
    public RequestController(CreateRequestUseCase createUC,
                             ApproveRequestUseCase approveUC,
                             RejectRequestUseCase rejectUC,
                             GetRequestQuery getQuery,
                             ListRequestsQuery listQuery,
                             GetHistoryQuery historyQuery,
                             RequestRestMapper mapper) {
        this.createUC = createUC;
        this.approveUC = approveUC;
        this.rejectUC = rejectUC;
        this.getQuery = getQuery;
        this.listQuery = listQuery;
        this.historyQuery = historyQuery;
        this.mapper = mapper;
    }

    private String currentUpn(Authentication auth) {
        if (auth instanceof JwtAuthenticationToken jat) {
            Map<String, Object> c = jat.getToken().getClaims();
            Object upn = c.get("upn");
            if (upn == null) upn = c.get("preferred_username");
            if (upn == null) upn = c.get("email");
            if (upn == null) upn = c.get("oid"); // último recurso
            if (upn != null) return String.valueOf(upn);
        }

        return (auth != null ? auth.getName() : "unknown");
    }

    private static String coalesce(String a, String b) {
        return (a != null && !a.isBlank()) ? a : b;
    }



    @PostMapping
    public ResponseEntity<RequestDetailDto> create(@RequestBody CreateRequestDto dto,
                                                   Authentication auth) {

        String requesterUpn = currentUpn(auth);

        ApprovalRequest created = createUC.create(
                dto.title(),
                dto.description(),
                requesterUpn,
                dto.approverUpn(),
                dto.type()
        );

        List<ApprovalAction> history = historyQuery.history(created.getId());

        return ResponseEntity.ok(
                mapper.toDetail(created, mapper.toActionDtos(history))
        );
    }

    @GetMapping
    public ResponseEntity<List<RequestSummaryDto>> list(
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String requesterUpn,
            @RequestParam(required = false) String approverUpn,
            @RequestParam(required = false) String createdFrom,
            @RequestParam(required = false) String createdTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {

        String effectiveRequester = requesterUpn;
        String effectiveApprover = approverUpn;

        List<ApprovalRequest> result = listQuery.list(
                status,
                type,
                effectiveRequester,
                effectiveApprover,
                createdFrom,
                createdTo,
                page,
                size
        );

        List<RequestSummaryDto> dto = result.stream()
                .map(mapper::toSummary)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RequestDetailDto> get(@PathVariable UUID id) {
        ApprovalRequest req = getQuery.get(id);
        List<ApprovalAction> history = historyQuery.history(id);
        return ResponseEntity.ok(
                mapper.toDetail(req, mapper.toActionDtos(history))
        );
    }

    @GetMapping("/inbox")
    public ResponseEntity<List<RequestSummaryDto>> inbox(@RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "20") int size,
                                                         Authentication auth) {
        String me = currentUpn(auth);
        List<ApprovalRequest> result = listQuery.list(
                null,
                null,
                null,
                me,
                null, null,
                page, size
        );
        List<RequestSummaryDto> dto = result.stream()
                .map(mapper::toSummary)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<RequestDetailDto> approve(@PathVariable UUID id,
                                                    @RequestBody(required = false) Map<String, Object> body,
                                                    Authentication auth) {
        String actorUpn = currentUpn(auth);
        String comment = extractComment(body);

        ApprovalRequest updated = approveUC.approve(id, actorUpn, comment);

        List<ApprovalAction> history = historyQuery.history(id);
        return ResponseEntity.ok(
                mapper.toDetail(updated, mapper.toActionDtos(history))
        );
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<RequestDetailDto> reject(@PathVariable UUID id,
                                                   @RequestBody Map<String, Object> body,
                                                   Authentication auth) {
        String actorUpn = currentUpn(auth);
        String comment = extractComment(body);

        if (comment == null || comment.isBlank()) {

            throw new DomainException("Reject requires a non-empty comment");
        }

        ApprovalRequest updated = rejectUC.reject(id, actorUpn, comment);

        List<ApprovalAction> history = historyQuery.history(id);
        return ResponseEntity.ok(
                mapper.toDetail(updated, mapper.toActionDtos(history))
        );
    }

    @SuppressWarnings("unchecked")
    private String extractComment(Map<String, Object> body) {
        if (body == null) return null;
        Object c1 = body.get("comment");
        if (c1 == null) c1 = body.get("reason");
        return (c1 != null ? String.valueOf(c1) : null);
    }
}
