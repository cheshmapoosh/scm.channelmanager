package ir.daneshrefah.scm.common.data.mapper;

import ir.daneshrefah.scm.common.data.entity.asset.MembershipTerminalServiceAccessEntity;
import ir.daneshrefah.scm.common.dto.asset.MembershipTerminalServiceAccess;
import ir.daneshrefah.scm.common.dto.asset.MembershipTerminalServiceAccessDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Objects;

@Mapper(uses = {ChannelServiceAccessMapper.class, MembershipTerminalAccessMapper.class, PersonMapper.class})
public interface MembershipTerminalServiceAccessMapper {
    MembershipTerminalServiceAccessMapper INSTANCE = Mappers.getMapper(MembershipTerminalServiceAccessMapper.class);

    MembershipTerminalServiceAccess toModel(MembershipTerminalServiceAccessEntity entity);

    default MembershipTerminalServiceAccessDto toDto(MembershipTerminalServiceAccessEntity entity) {
        if (Objects.isNull(entity)) {
            return null;
        }
        MembershipTerminalServiceAccessDto result = new MembershipTerminalServiceAccessDto();
        result.setEbService(EbServiceMapper.INSTANCE.toModel(entity.getChannelServiceAccess().getEbService()));
        result.setMaxWithdrawalPerTransaction(entity.getMaxWithdrawalPerTransaction().toPlainString());
        return result;
    }

}
