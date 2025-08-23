package co.com.bancodebogota.bdbapprovals.application.port.out;

import co.com.bancodebogota.bdbapprovals.domain.model.ApprovalRequest;

public interface NotificationPort {
    void sendRequestCreated(ApprovalRequest req);
    void sendRequestApproved(ApprovalRequest req, String comment);
    void sendRequestRejected(ApprovalRequest req, String comment);
}
