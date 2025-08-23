package co.com.bancodebogota.bdbapprovals.infrastructure.rest;

import co.com.bancodebogota.bdbapprovals.application.port.in.*;
import co.com.bancodebogota.bdbapprovals.domain.model.*;
import co.com.bancodebogota.bdbapprovals.infrastructure.rest.dto.CreateRequestDto;
import co.com.bancodebogota.bdbapprovals.infrastructure.rest.mapper.RequestRestMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RequestController.class)
public class RequestControllerTest {

    @Autowired MockMvc mvc;

    @MockBean CreateRequestUseCase createUC;
    @MockBean ApproveRequestUseCase approveUC;
    @MockBean RejectRequestUseCase rejectUC;
    @MockBean CommentRequestUseCase commentUC;
    @MockBean GetRequestQuery getQuery;
    @MockBean ListRequestsQuery listQuery;
    @MockBean GetHistoryQuery historyQuery;
    @MockBean GetInboxQuery inboxQuery;
    @MockBean RequestRestMapper mapper;

    @Test
    @WithAnonymousUser
    void create_requires_auth_but_mock_filter_will_set_it_in_full_context_tests() throws Exception {
        var id = UUID.randomUUID();
        var req = new ApprovalRequest(id,"T","D","r@o365","a@o365","DEPLOY", RequestStatus.PENDING, Instant.now(), Instant.now());
        Mockito.when(createUC.create(any(),any(),any(),any(),any())).thenReturn(req);
        Mockito.when(mapper.toSummary(any())).thenCallRealMethod(); // si generas implementación, ajusta esto

        mvc.perform(post("/api/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
              {"title":"T","description":"D","approverUpn":"a@o365","type":"DEPLOY"}
            """)
                        .header("X-User-UPN","r@o365"))
                .andExpect(status().isOk());
    }
}
