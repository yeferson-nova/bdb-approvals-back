package co.com.bancodebogota.bdbapprovals.infrastructure.persistence.mapper;

import co.com.bancodebogota.bdbapprovals.domain.model.ApprovalAction;
import co.com.bancodebogota.bdbapprovals.infrastructure.persistence.entity.ApprovalActionEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ActionEntityMapper {
    ApprovalActionEntity toEntity(ApprovalAction domain);
    ApprovalAction toDomain(ApprovalActionEntity entity);
}
