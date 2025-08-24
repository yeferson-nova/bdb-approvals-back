package co.com.bancodebogota.bdbapprovals.application.port.out;

import co.com.bancodebogota.bdbapprovals.domain.model.ApprovalRequest;

public interface EmailPort {
    void sendRequestCreated(ApprovalRequest request);
    void sendRequestApproved(ApprovalRequest request, String comment);
    void sendRequestRejected(ApprovalRequest request, String comment);
}