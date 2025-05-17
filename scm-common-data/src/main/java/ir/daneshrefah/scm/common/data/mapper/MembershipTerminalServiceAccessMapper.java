package ir.daneshrefah.scm.common.data.mapper;

import ir.daneshrefah.scm.common.data.entity.asset.MembershipTerminalServiceAccessEntity;
import ir.daneshrefah.scm.common.dto.asset.MembershipTerminalServiceAccess;
import ir.daneshrefah.scm.common.dto.asset.MembershipTerminalServiceAccessDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.Objects;

@Mapper(uses = {ChannelServiceAccessMapper.class, MembershipTerminalAccessMapper.class, PersonMapper.class}, componentModel = MappingConstants.ComponentModel.SPRING)
public interface MembershipTerminalServiceAccessMapper {

    MembershipTerminalServiceAccess toModel(MembershipTerminalServiceAccessEntity entity);

    default MembershipTerminalServiceAccessDto toDto(MembershipTerminalServiceAccessEntity entity) {
        if (Objects.isNull(entity)) {
            return null;
        }
        MembershipTerminalServiceAccessDto result = new MembershipTerminalServiceAccessDto();
        //TODO SCMNEW-4: Resolve by Dariush
//        result.setEbService(EbServiceMapper.INSTANCE.toModel(entity.getChannelServiceAccess().getEbService()));
        result.setMaxWithdrawalPerTransaction(entity.getMaxWithdrawalPerTransaction().toString());
        result.setId(entity.getId());
        return result;
    }

}
