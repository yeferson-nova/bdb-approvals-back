package co.com.bancodebogota.bdbapprovals.infrastructure.rest;

import co.com.bancodebogota.bdbapprovals.infrastructure.rest.dto.CurrentUserDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void me_returns_current_user_from_security_context() throws Exception {
        String userUpn = "test.user@example.com";
        String userOid = "some-oid";
        String userName = "Test User";
        String userEmail = "test.user@example.com";

        var currentUserDto = new CurrentUserDto(userUpn, userOid, userName, userEmail);

        mvc.perform(get("/api/auth/me")
                        .header("X-User-UPN", userUpn)
                        .header("X-User-OID", userOid)
                        .header("X-User-Name", userName)
                        .header("X-User-Email", userEmail))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(currentUserDto)));
    }
}
