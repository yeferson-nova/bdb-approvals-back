package co.com.bancodebogota.bdbapprovals.infrastructure.rest;

import co.com.bancodebogota.bdbapprovals.application.port.in.*;
import co.com.bancodebogota.bdbapprovals.domain.exception.DomainException;
import co.com.bancodebogota.bdbapprovals.domain.model.ApprovalAction;
import co.com.bancodebogota.bdbapprovals.domain.model.ApprovalRequest;
import co.com.bancodebogota.bdbapprovals.domain.model.RequestStatus;
import co.com.bancodebogota.bdbapprovals.infrastructure.rest.dto.ActionDto;
import co.com.bancodebogota.bdbapprovals.infrastructure.rest.dto.CreateRequestDto;
import co.com.bancodebogota.bdbapprovals.infrastructure.rest.dto.RequestDetailDto;
import co.com.bancodebogota.bdbapprovals.infrastructure.rest.dto.RequestSummaryDto;
import co.com.bancodebogota.bdbapprovals.infrastructure.rest.dto.CurrentUserDto;
import co.com.bancodebogota.bdbapprovals.infrastructure.rest.mapper.RequestRestMapper;
import co.com.bancodebogota.bdbapprovals.infrastructure.security.CurrentUserParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping
    public ResponseEntity<RequestDetailDto> create(@RequestBody CreateRequestDto dto,
                                                   @CurrentUserParam CurrentUserDto me) {

        ApprovalRequest created = createUC.create(
                dto.title(),
                dto.description(),
                me.upn(),              // 🔒 requester sale del JWT
                dto.approverUpn(),
                dto.type()
        );

        List<ApprovalAction> history = historyQuery.history(created.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toDetail(created, mapper.toActionDtos(history)));
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
            @RequestParam(defaultValue = "20") int size) {

        List<ApprovalRequest> result = listQuery.list(
                status,
                type,
                requesterUpn,
                approverUpn,
                createdFrom,
                createdTo,
                page,
                size
        );

        return ResponseEntity.ok(mapToSummaryDtos(result));
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
                                                         @CurrentUserParam CurrentUserDto me) {
        List<ApprovalRequest> result = listQuery.list(
                null,
                null,
                null,
                me.upn(),      // 🔒 bandeja del aprobador logueado
                null, null,
                page, size
        );
        return ResponseEntity.ok(mapToSummaryDtos(result));
    }

    @GetMapping("/outbox")
    public ResponseEntity<List<RequestSummaryDto>> outbox(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "20") int size,
                                                          @CurrentUserParam CurrentUserDto me) {
        List<ApprovalRequest> result = listQuery.list(
                null,
                null,
                me.upn(),      // 🔒 bandeja del solicitante logueado
                null,
                null, null,
                page, size
        );
        return ResponseEntity.ok(mapToSummaryDtos(result));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<RequestDetailDto> approve(@PathVariable UUID id,
                                                    @RequestBody(required = false) ActionDto body,
                                                    @CurrentUserParam CurrentUserDto me) {
        String comment = extractComment(body);

        ApprovalRequest updated = approveUC.approve(id, me.upn(), comment);

        return buildDetailResponse(updated);
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<RequestDetailDto> reject(@PathVariable UUID id,
                                                   @RequestBody ActionDto body,
                                                   @CurrentUserParam CurrentUserDto me) {
        String comment = extractComment(body);

        if (comment == null || comment.isBlank()) {
            throw new DomainException("Reject requires a non-empty comment");
        }

        ApprovalRequest updated = rejectUC.reject(id, me.upn(), comment);

        return buildDetailResponse(updated);
    }


    private String extractComment(ActionDto body) {
        if (body == null) return null;
        return Optional.ofNullable(body.comment())
                .filter(s -> !s.isBlank())
                .orElse(null);
    }

    private ResponseEntity<RequestDetailDto> buildDetailResponse(ApprovalRequest request) {
        List<ApprovalAction> history = historyQuery.history(request.getId());
        RequestDetailDto dto = mapper.toDetail(request, mapper.toActionDtos(history));
        return ResponseEntity.ok(dto);
    }

    private List<RequestSummaryDto> mapToSummaryDtos(List<ApprovalRequest> requests) {
        return requests.stream()
                .map(mapper::toSummary)
                .collect(Collectors.toList());
    }
}
