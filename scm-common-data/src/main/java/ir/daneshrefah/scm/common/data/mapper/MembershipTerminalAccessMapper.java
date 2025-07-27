package ir.daneshrefah.scm.common.data.mapper;

import ir.daneshrefah.scm.common.data.entity.asset.MembershipTerminalAccessEntity;
import ir.daneshrefah.scm.common.dto.asset.MembershipTerminalAccessDto;
import ir.daneshrefah.scm.common.model.asset.MembershipTerminalAccess;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-25
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {CmChannelMapper.class, MembershipMapper.class, PersonMapper.class})
public abstract class MembershipTerminalAccessMapper {

    @Autowired
    private MembershipMapper membershipMapper;

    public abstract MembershipTerminalAccess toModel(MembershipTerminalAccessEntity entity);

    public abstract MembershipTerminalAccessEntity toEntity(MembershipTerminalAccess model);

    public MembershipTerminalAccessDto toDto(MembershipTerminalAccessEntity entity) {
        return toDto(toModel(entity));
    }

    public MembershipTerminalAccessDto toDto(MembershipTerminalAccess model) {
        if (Objects.isNull(model)) return null;
        MembershipTerminalAccessDto dto = new MembershipTerminalAccessDto();
        dto.setActive(model.getActive());
        dto.setFavorite(model.getFavorite());
        dto.setMaxWithdrawalPerDay(StringUtils.EMPTY);
        dto.setMaxPersWithdrawalPerDay(StringUtils.EMPTY);
        if (Objects.nonNull(model.getMaxWithdrawalPerDay())) {
            dto.setMaxWithdrawalPerDay(model.getMaxWithdrawalPerDay().toPlainString());
        }
        if (Objects.nonNull(model.getMaxPersWithdrawalPerDay())) {
            dto.setMaxPersWithdrawalPerDay(model.getMaxPersWithdrawalPerDay().toPlainString());
        }
        dto.setFromDate(model.getFromDate());
        dto.setToDate(model.getToDate());
        dto.setId(model.getId());
        dto.setReason(model.getReason());
        dto.setUserReason(model.getUserReason());
        dto.setCreator(model.getCreator());
        dto.setLastEditor(model.getLastEditor());
        dto.setCreateDate(model.getCreateDate());
        dto.setLastEditDate(model.getLastEditDate());
        dto.setMembership(membershipMapper.toDto(model.getMembership()));
        return dto;
    }
}
