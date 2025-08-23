package co.com.bancodebogota.bdbapprovals.infrastructure.persistence.mapper;

import co.com.bancodebogota.bdbapprovals.domain.model.ApprovalRequest;
import co.com.bancodebogota.bdbapprovals.infrastructure.persistence.entity.ApprovalRequestEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RequestEntityMapper {
    ApprovalRequestEntity toEntity(ApprovalRequest domain);
    @Mapping(target = "status", source = "status")
    ApprovalRequest toDomain(ApprovalRequestEntity entity);
}
