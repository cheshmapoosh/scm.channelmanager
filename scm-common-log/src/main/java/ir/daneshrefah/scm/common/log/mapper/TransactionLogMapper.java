package ir.daneshrefah.scm.common.log.mapper;

import ir.daneshrefah.scm.common.constant.TerminalType;
import ir.daneshrefah.scm.common.log.entity.transaction.TransactionLogEntity;
import ir.daneshrefah.scm.common.log.model.TransactionLogResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface TransactionLogMapper {

    TransactionLogMapper INSTANCE = Mappers.getMapper(TransactionLogMapper.class);

    @Mappings({
            @Mapping(source = "id.transactionLogId", target = "transactionLogId"),
            @Mapping(source = "id.archiveNo", target = "archiveNo"),
            @Mapping(target = "channelCode", expression = "java(convertTerminalId(entity.getChannelId()))")    })
    TransactionLogResponse toModel(TransactionLogEntity entity);

    default List<TransactionLogResponse> toModelList(List<TransactionLogEntity> entities) {
        if (null == entities || entities.isEmpty()) return null;
        return entities.stream().map(this::toModel).toList();
    }

    default String convertTerminalId(Integer legacyTerminalId) {
        if (legacyTerminalId == null) return null;
        TerminalType terminalType = TerminalType.findByLegacyTerminalCode(legacyTerminalId);
        return terminalType != null ? terminalType.getTerminalCode() : null;

    }
}
