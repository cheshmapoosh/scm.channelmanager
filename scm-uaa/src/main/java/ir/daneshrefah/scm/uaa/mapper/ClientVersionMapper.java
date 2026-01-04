package ir.daneshrefah.scm.uaa.mapper;

import ir.daneshrefah.scm.uaa.domain.client.ClientVersion;
import ir.daneshrefah.scm.uaa.repository.authentication.client.entity.ClientVersionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Objects;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.IGNORE;

@Mapper(unmappedTargetPolicy =IGNORE, componentModel = SPRING)
public interface ClientVersionMapper {


    @Mapping(target = "clientId",expression = "java(getClientId(entity))")
    @Mapping(target = "appVersion", source = "entity.version")
    @Mapping(target = "url", constant = "https://newmob.rkbank.ir")
    ClientVersion toModel(ClientVersionEntity entity);
    @Mapping(target = "version", source = "model.appVersion")
    ClientVersionEntity toEntity(ClientVersion model);

    default Long getClientId(ClientVersionEntity entity){
        if (Objects.nonNull(entity) && Objects.nonNull(entity.getClient())){
            return entity.getClient().getId();
        }
        return null;
    }
}
