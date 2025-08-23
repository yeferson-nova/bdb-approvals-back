// src/test/java/.../domain/ApprovalRequestTest.java
package co.com.bancodebogota.bdbapprovals.domain;

import co.com.bancodebogota.bdbapprovals.domain.exception.DomainException;
import co.com.bancodebogota.bdbapprovals.domain.model.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ApprovalRequestTest {

    private ApprovalRequest newPending() {
        return new ApprovalRequest(UUID.randomUUID(), "Titulo", "Desc", "req@o365", "apr@o365", "DEPLOY",
                RequestStatus.PENDING, Instant.now(), Instant.now());
    }

    @Test
    void approve_from_pending_ok() {
        var r = newPending();
        var a = r.approve("apr@o365", "ok", Instant.now());
        assertEquals(RequestStatus.APPROVED, r.getStatus());
        assertEquals(ActionType.APPROVE, a.action());
    }

    @Test
    void reject_requires_comment() {
        var r = newPending();
        assertThrows(DomainException.class, () -> r.reject("apr@o365", "", Instant.now()));
    }

    @Test
    void approve_when_not_pending_fails() {
        var r = newPending();
        r.approve("apr@o365", "ok", Instant.now());
        assertThrows(DomainException.class, () -> r.reject("apr@o365", "late", Instant.now()));
    }
}
