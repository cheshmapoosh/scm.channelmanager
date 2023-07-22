package ir.daneshrefah.scm.mapper;

import ir.daneshrefah.scm.common.model.Profile;
import ir.daneshrefah.scm.entity.ProfileEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProfileMapper {
    ProfileMapper INSTANCE = Mappers.getMapper(ProfileMapper.class);

    Profile toModel(ProfileEntity entity);

}
