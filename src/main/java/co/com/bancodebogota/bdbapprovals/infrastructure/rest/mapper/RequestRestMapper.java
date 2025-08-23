package co.com.bancodebogota.bdbapprovals.infrastructure.rest.mapper;

import co.com.bancodebogota.bdbapprovals.domain.model.ApprovalRequest;
import co.com.bancodebogota.bdbapprovals.infrastructure.rest.dto.RequestSummaryDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RequestRestMapper {
    RequestSummaryDto toSummary(ApprovalRequest req);
}
