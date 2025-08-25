package co.com.bancodebogota.bdbapprovals.infrastructure.rest;

import co.com.bancodebogota.bdbapprovals.application.port.in.*;
import co.com.bancodebogota.bdbapprovals.domain.model.ApprovalAction;
import co.com.bancodebogota.bdbapprovals.domain.model.ApprovalRequest;
import co.com.bancodebogota.bdbapprovals.domain.model.RequestStatus;
import co.com.bancodebogota.bdbapprovals.infrastructure.rest.dto.ActionDto;
import co.com.bancodebogota.bdbapprovals.infrastructure.rest.dto.CreateRequestDto;
import co.com.bancodebogota.bdbapprovals.infrastructure.rest.dto.RequestDetailDto;
import co.com.bancodebogota.bdbapprovals.infrastructure.rest.dto.RequestSummaryDto;
import co.com.bancodebogota.bdbapprovals.infrastructure.rest.mapper.RequestRestMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RequestController.class)
class RequestControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateRequestUseCase createUC;
    @MockBean
    private ApproveRequestUseCase approveUC;
    @MockBean
    private RejectRequestUseCase rejectUC;
    @MockBean
    private GetRequestQuery getQuery;
    @MockBean
    private ListRequestsQuery listQuery;
    @MockBean
    private GetHistoryQuery historyQuery;
    @MockBean
    private RequestRestMapper mapper;

    private static final String FAKE_USER_UPN = "user@example.com";

    @Test
    void create_returns_201_with_request_details() throws Exception {
        var createDto = new CreateRequestDto("Test Title", "Test Description", "approver@example.com", "DEPLOY");
        var requestId = UUID.randomUUID();
        var now = Instant.now();
        var approvalRequest = new ApprovalRequest(requestId, createDto.title(), createDto.description(), FAKE_USER_UPN, createDto.approverUpn(), createDto.type(), RequestStatus.PENDING, now, now);
        var history = Collections.<ApprovalAction>emptyList();
        var detailDto = new RequestDetailDto(requestId, createDto.title(), FAKE_USER_UPN, createDto.approverUpn(), createDto.type(), RequestStatus.PENDING, now, now, Collections.emptyList());

        when(createUC.create(any(), any(), any(), any(), any())).thenReturn(approvalRequest);
        when(historyQuery.history(requestId)).thenReturn(history);
        when(mapper.toDetail(approvalRequest, Collections.emptyList())).thenReturn(detailDto);

        mvc.perform(post("/api/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto))
                        .header("X-User-UPN", FAKE_USER_UPN))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(detailDto)));
    }

    @Test
    void get_returns_200_with_request_details() throws Exception {
        var requestId = UUID.randomUUID();
        var now = Instant.now();
        var approvalRequest = new ApprovalRequest(requestId, "Title", "Description", "requester@a.com", "approver@a.com", "TYPE", RequestStatus.PENDING, now, now);
        var history = Collections.<ApprovalAction>emptyList();
        var detailDto = new RequestDetailDto(requestId, "Title", "requester@a.com", "approver@a.com", "TYPE", RequestStatus.PENDING, now, now, Collections.emptyList());

        when(getQuery.get(requestId)).thenReturn(approvalRequest);
        when(historyQuery.history(requestId)).thenReturn(history);
        when(mapper.toDetail(approvalRequest, Collections.emptyList())).thenReturn(detailDto);
        mvc.perform(get("/api/requests/{id}", requestId))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(detailDto)));
    }

    @Test
    void approve_returns_200_with_updated_details() throws Exception {
        var requestId = UUID.randomUUID();
        var now = Instant.now();
        var actionDto = new ActionDto(null, null, null, "Approved!", null);
        var updatedRequest = new ApprovalRequest(requestId, "Title", "Description", "req@a.com", FAKE_USER_UPN, "TYPE", RequestStatus.APPROVED, now, now);
        var history = Collections.<ApprovalAction>emptyList();
        var detailDto = new RequestDetailDto(requestId, "Title", "req@a.com", FAKE_USER_UPN, "TYPE", RequestStatus.APPROVED, now, now, Collections.emptyList());

        when(approveUC.approve(requestId, FAKE_USER_UPN, "Approved!")).thenReturn(updatedRequest);
        when(historyQuery.history(requestId)).thenReturn(history);
        when(mapper.toDetail(updatedRequest, Collections.emptyList())).thenReturn(detailDto);
        when(mapper.toActionDtos(any())).thenReturn(Collections.emptyList());
        mvc.perform(post("/api/requests/{id}/approve", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actionDto))
                        .header("X-User-UPN", FAKE_USER_UPN))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(detailDto)));
    }

    @Test
    void reject_returns_200_with_updated_details() throws Exception {
        var requestId = UUID.randomUUID();
        var now = Instant.now();
        var actionDto = new ActionDto(null, null, null, "Rejected!", null);
        var updatedRequest = new ApprovalRequest(requestId, "Title", "Description", "req@a.com", FAKE_USER_UPN, "TYPE", RequestStatus.REJECTED, now, now);
        var history = Collections.<ApprovalAction>emptyList();
        var detailDto = new RequestDetailDto(requestId, "Title", "req@a.com", FAKE_USER_UPN, "TYPE", RequestStatus.REJECTED, now, now, Collections.emptyList());

        when(rejectUC.reject(requestId, FAKE_USER_UPN, "Rejected!")).thenReturn(updatedRequest);
        when(historyQuery.history(requestId)).thenReturn(history);
        when(mapper.toDetail(updatedRequest, Collections.emptyList())).thenReturn(detailDto);
        when(mapper.toActionDtos(any())).thenReturn(Collections.emptyList());

        mvc.perform(post("/api/requests/{id}/reject", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actionDto))
                        .header("X-User-UPN", FAKE_USER_UPN))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(detailDto)));
    }

    @Test
    void list_returns_200_with_summary_list() throws Exception {
        var now = Instant.now();
        var approvalRequest = new ApprovalRequest(UUID.randomUUID(), "Title", "Desc", "req@a.com", "app@a.com", "TYPE", RequestStatus.PENDING, now, now);
        var summaryDto = new RequestSummaryDto(approvalRequest.getId(), "Title", "req@a.com", "app@a.com", "TYPE", RequestStatus.PENDING, now, now);
        var requestList = List.of(approvalRequest);
        var summaryList = List.of(summaryDto);

        when(listQuery.list(any(), any(), any(), any(), any(), any(), any(Integer.class), any(Integer.class))).thenReturn(requestList);
        when(mapper.toSummary(approvalRequest)).thenReturn(summaryDto);

        mvc.perform(get("/api/requests").param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(summaryList)));
    }

    @Test
    void inbox_returns_200_with_summary_list_for_current_user() throws Exception {
        var now = Instant.now();
        var approvalRequest = new ApprovalRequest(UUID.randomUUID(), "Title", "Desc", "req@a.com", FAKE_USER_UPN, "TYPE", RequestStatus.PENDING, now, now);
        var summaryDto = new RequestSummaryDto(approvalRequest.getId(), "Title", "req@a.com", FAKE_USER_UPN, "TYPE", RequestStatus.PENDING, now, now);
        var requestList = List.of(approvalRequest);
        var summaryList = List.of(summaryDto);

        when(listQuery.list(null, null, null, FAKE_USER_UPN, null, null, 0, 20)).thenReturn(requestList);
        when(mapper.toSummary(approvalRequest)).thenReturn(summaryDto);

        mvc.perform(get("/api/requests/inbox").header("X-User-UPN", FAKE_USER_UPN))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(summaryList)));
    }

    @Test
    void outbox_returns_200_with_summary_list_for_current_user() throws Exception {
        var now = Instant.now();
        var approvalRequest = new ApprovalRequest(UUID.randomUUID(), "Title", "Desc", FAKE_USER_UPN, "app@a.com", "TYPE", RequestStatus.PENDING, now, now);
        var summaryDto = new RequestSummaryDto(approvalRequest.getId(), "Title", FAKE_USER_UPN, "app@a.com", "TYPE", RequestStatus.PENDING, now, now);
        var requestList = List.of(approvalRequest);
        var summaryList = List.of(summaryDto);

        when(listQuery.list(null, null, FAKE_USER_UPN, null, null, null, 0, 20)).thenReturn(requestList);
        when(mapper.toSummary(approvalRequest)).thenReturn(summaryDto);

        mvc.perform(get("/api/requests/outbox").header("X-User-UPN", FAKE_USER_UPN))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(summaryList)));
    }
}