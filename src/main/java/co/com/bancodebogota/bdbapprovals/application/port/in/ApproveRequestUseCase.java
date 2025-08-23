package co.com.bancodebogota.bdbapprovals.application.port.in;

import co.com.bancodebogota.bdbapprovals.domain.model.ApprovalRequest;
import java.util.UUID;

public interface ApproveRequestUseCase {
    ApprovalRequest approve(UUID id, String actorUpn, String comment);
}