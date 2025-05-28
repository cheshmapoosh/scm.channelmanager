package ir.daneshrefah.scm.common.data.mapper;

import ir.daneshrefah.scm.common.data.entity.asset.MembershipTerminalServiceAccessEntity;
import ir.daneshrefah.scm.common.dto.asset.MembershipTerminalServiceAccess;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(uses = {ChannelServiceAccessMapper.class,
        MembershipTerminalAccessMapper.class,
        PersonMapper.class},
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface MembershipTerminalServiceAccessMapper {

    MembershipTerminalServiceAccess toModel(MembershipTerminalServiceAccessEntity entity);



}
