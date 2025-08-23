package co.com.bancodebogota.bdbapprovals.application.port.in;

import co.com.bancodebogota.bdbapprovals.domain.model.ApprovalRequest;
import co.com.bancodebogota.bdbapprovals.domain.model.RequestStatus;
import java.util.List;

public interface ListRequestsQuery {
    List<ApprovalRequest> list(RequestStatus status, String type, String requesterUpn, String approverUpn,
                               String createdFrom, String createdTo, int page, int size);
}