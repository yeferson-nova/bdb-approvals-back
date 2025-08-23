package co.com.bancodebogota.bdbapprovals.application.port.in;

import co.com.bancodebogota.bdbapprovals.domain.model.ApprovalRequest;
import java.util.UUID;

public interface GetRequestQuery {
    ApprovalRequest get(UUID id);
}