package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.common.model.authority.Authority;
import ir.daneshrefah.scm.common.model.authority.terminal.TerminalServiceAccessAuthority;
import ir.daneshrefah.scm.common.model.authority.terminal.TerminalWithdrawAuthority;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.core.entity.authority.AuthorityEntity;
import ir.daneshrefah.scm.core.entity.authority.TerminalServiceAccessAuthorityEntity;
import ir.daneshrefah.scm.core.entity.authority.TerminalWithdrawAuthorityEntity;
import ir.daneshrefah.scm.core.entity.service.ServiceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mapper
public interface AuthorityMapper {
    AuthorityMapper INSTANCE = Mappers.getMapper(AuthorityMapper.class);

    @Mapping(source = "sourceService", target = "sourceService", qualifiedByName = "mapService")
    TerminalServiceAccessAuthority toModel(TerminalServiceAccessAuthorityEntity entity);

    List<TerminalServiceAccessAuthority> terminalServiceAccessEntitiesToModels(Iterable<TerminalServiceAccessAuthorityEntity> entities);

//    TerminalServiceAccessAuthorityEntity toEntity(TerminalServiceAccessAuthority model);

    @Mapping(source = "sourceService", target = "sourceService", qualifiedByName = "mapService")
    TerminalWithdrawAuthority toModel(TerminalWithdrawAuthorityEntity entity);

    List<TerminalWithdrawAuthority> terminalWithdrawEntitiesToModels(Iterable<TerminalWithdrawAuthorityEntity> entities);

//    TerminalWithdrawAuthorityEntity toEntity(TerminalWithdrawAuthority model);


    @Named("toAuthority")
    default Authority toAuthority(AuthorityEntity authorityEntity) {
        if (authorityEntity instanceof TerminalServiceAccessAuthorityEntity) {
            return toModel((TerminalServiceAccessAuthorityEntity) authorityEntity);
        } else if (authorityEntity instanceof TerminalWithdrawAuthorityEntity) {
            return toModel((TerminalWithdrawAuthorityEntity) authorityEntity);
        }
        return null;
    }

    @Named("toAuthorities")
    default List<Authority> toAuthorities(Iterable<AuthorityEntity> authorityEntities) {
        if (null == authorityEntities)
            return null;
        List<Authority> result = new ArrayList<>();
        for (Iterator<AuthorityEntity> iterator = authorityEntities.iterator(); iterator.hasNext(); ) {
            AuthorityEntity serviceEntity = iterator.next();
            Authority authority = toAuthority(serviceEntity);
            result.add(authority);
        }
        return result;
    }

    @Named("mapService")
    default Service mapService(ServiceEntity entity) {
        // Delegate the mapping to the method in ServiceMapper
        return ServiceMapper.INSTANCE.toService(entity);
    }

}
