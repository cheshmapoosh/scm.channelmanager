package ir.daneshrefah.scm.config.model.mapper;

import ir.daneshrefah.scm.config.model.application.ApplicationDTO;
import ir.daneshrefah.scm.config.model.entity.ApplicationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ApplicationMapper {

    ApplicationMapper INSTANCE = Mappers.getMapper(ApplicationMapper.class);

    List<ApplicationDTO> toApplicationsModel(List<ApplicationEntity> applicationEntities);

}
