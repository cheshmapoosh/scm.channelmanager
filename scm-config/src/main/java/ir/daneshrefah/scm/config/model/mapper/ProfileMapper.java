package ir.daneshrefah.scm.config.model.mapper;

import ir.daneshrefah.scm.config.model.profile.ProfileDTO;
import ir.daneshrefah.scm.config.model.entity.ProfileEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ProfileMapper {

    ProfileMapper INSTANCE = Mappers.getMapper(ProfileMapper.class);

    List<ProfileDTO> toProfilesDTO(List<ProfileEntity> profileEntities);
}
