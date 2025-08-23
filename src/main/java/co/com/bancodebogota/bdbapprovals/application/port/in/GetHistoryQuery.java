package co.com.bancodebogota.bdbapprovals.application.port.in;

import co.com.bancodebogota.bdbapprovals.domain.model.ApprovalAction;
import java.util.List;
import java.util.UUID;

public interface GetHistoryQuery {
    List<ApprovalAction> history(UUID id);
}