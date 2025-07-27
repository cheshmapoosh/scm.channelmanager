package ir.daneshrefah.scm.common.data.mapper;

import ir.daneshrefah.scm.common.data.entity.asset.MembershipTerminalServiceAccessEntity;
import ir.daneshrefah.scm.common.dto.asset.MembershipTerminalServiceAccess;
import ir.daneshrefah.scm.common.dto.asset.MembershipTerminalServiceAccessDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {ChannelServiceAccessMapper.class, MembershipTerminalAccessMapper.class, PersonMapper.class})
public abstract class MembershipTerminalServiceAccessMapper {

    @Autowired
    private ServiceMapper serviceMapper;

    public abstract MembershipTerminalServiceAccess toModel(MembershipTerminalServiceAccessEntity entity);

    public MembershipTerminalServiceAccessDto toDto(MembershipTerminalServiceAccessEntity entity) {
        if (Objects.isNull(entity)) {
            return null;
        }
        MembershipTerminalServiceAccessDto result = new MembershipTerminalServiceAccessDto();
        result.setEbService(serviceMapper.toModel(entity.getChannelServiceAccess().getService()));
        result.setMaxWithdrawalPerTransaction(entity.getMaxWithdrawalPerTransaction().toString());
        result.setId(entity.getId());
        return result;
    }

}
