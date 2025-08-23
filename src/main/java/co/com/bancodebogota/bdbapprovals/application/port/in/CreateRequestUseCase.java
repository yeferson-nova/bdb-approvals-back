package co.com.bancodebogota.bdbapprovals.application.port.in;

import co.com.bancodebogota.bdbapprovals.domain.model.ApprovalRequest;

public interface CreateRequestUseCase {
    ApprovalRequest create(String title, String description, String requesterUpn, String approverUpn, String type);
}
