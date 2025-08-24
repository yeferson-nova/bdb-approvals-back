package co.com.bancodebogota.bdbapprovals.infrastructure.notification;

import co.com.bancodebogota.bdbapprovals.application.port.out.EmailPort;
import co.com.bancodebogota.bdbapprovals.domain.model.ApprovalRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev") // Se activa solo en el perfil 'dev'
public class LogEmailAdapter implements EmailPort {
    private static final Logger log = LoggerFactory.getLogger(LogEmailAdapter.class);
    @Override
    public void sendRequestCreated(ApprovalRequest request) {
        log.info("[EMAIL-MOCK] Email sending is disabled in 'dev' profile. Would send 'request_created' notification for request ID: {}", request.getId());
    }

    @Override
    public void sendRequestApproved(ApprovalRequest request, String comment) {
        log.info("[EMAIL-MOCK] Email sending is disabled in 'dev' profile. Would send 'request_approved' notification for request ID: {}", request.getId());
    }

    @Override
    public void sendRequestRejected(ApprovalRequest request, String comment) {
        log.info("[EMAIL-MOCK] Email sending is disabled in 'dev' profile. Would send 'request_rejected' notification for request ID: {}", request.getId());
    }
}