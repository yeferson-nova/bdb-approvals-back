package co.com.bancodebogota.bdbapprovals.application.port.in;

import co.com.bancodebogota.bdbapprovals.domain.model.ApprovalRequest;
import java.util.List;

public interface GetInboxQuery {
    List<ApprovalRequest> inbox(String approverUpn, int page, int size);
}