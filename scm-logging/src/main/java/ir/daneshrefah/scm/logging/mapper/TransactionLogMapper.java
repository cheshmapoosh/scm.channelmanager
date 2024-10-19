package ir.daneshrefah.scm.logging.mapper;

import ir.daneshrefah.scm.logging.entity.TransactionLogEntity;
import ir.daneshrefah.scm.logging.model.TransactionLogDetailResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TransactionLogMapper {

    TransactionLogMapper INSTANCE = Mappers.getMapper(TransactionLogMapper.class);

    TransactionLogDetailResponse toModel(TransactionLogEntity entity);
}
