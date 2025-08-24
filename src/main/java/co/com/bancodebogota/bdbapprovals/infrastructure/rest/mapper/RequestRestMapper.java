package co.com.bancodebogota.bdbapprovals.infrastructure.rest.mapper;

import co.com.bancodebogota.bdbapprovals.domain.model.ApprovalAction;
import co.com.bancodebogota.bdbapprovals.domain.model.ApprovalRequest;
import co.com.bancodebogota.bdbapprovals.infrastructure.rest.dto.ActionDto;
import co.com.bancodebogota.bdbapprovals.infrastructure.rest.dto.RequestDetailDto;
import co.com.bancodebogota.bdbapprovals.infrastructure.rest.dto.RequestSummaryDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface RequestRestMapper {

    RequestSummaryDto toSummary(ApprovalRequest req);

    @Mapping(target = "comments", source = "comments")
    RequestDetailDto toDetail(ApprovalRequest req, List<ActionDto> comments);
    default ActionDto toActionDto(ApprovalAction a) {
        if (a == null) return null;
        return new ActionDto(
                a.id(),
                a.actorUpn(),
                a.action(),
                a.comment(),
                a.occurredAt()
        );
    }

    default List<ActionDto> toActionDtos(List<ApprovalAction> actions) {
        return actions == null ? List.of()
                : actions.stream().map(this::toActionDto).collect(Collectors.toList());
    }
}
