package ir.daneshrefah.scm.common.data.mapper;

import ir.daneshrefah.scm.common.data.entity.asset.MembershipTerminalAccessEntity;
import ir.daneshrefah.scm.common.dto.asset.MembershipTerminalAccessDto;
import ir.daneshrefah.scm.common.model.asset.MembershipTerminalAccess;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;
import java.util.Objects;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-25
 */
@Mapper(uses = {ChannelMapper.class,MembershipMapper.class, PersonMapper.class}, componentModel = MappingConstants.ComponentModel.SPRING)
public interface MembershipTerminalAccessMapper {

    MembershipTerminalAccess toMembershipTerminalAccess(MembershipTerminalAccessEntity entity);

    List<MembershipTerminalAccess> toMembershipTerminalAccessList(Iterable<MembershipTerminalAccessEntity> entity);


//    @Named("toPerson")
//    default GeneralPerson toPerson(GeneralPersonEntity entity) {
//        return PersonMapper.INSTANCE.toPerson(entity);
//    }

    default MembershipTerminalAccessDto toDto(MembershipTerminalAccessEntity entity){
        return toDto(toMembershipTerminalAccess(entity));
    }

    default MembershipTerminalAccessDto toDto(MembershipTerminalAccess model) {
        if (Objects.isNull(model)) return null;
        MembershipTerminalAccessDto dto = new MembershipTerminalAccessDto();
        dto.setActive(model.getActive());
        dto.setFavorite(model.getFavorite());
        dto.setMaxWithdrawalPerDay(StringUtils.EMPTY);
        dto.setMaxPersWithdrawalPerDay(StringUtils.EMPTY);
        if (Objects.nonNull(model.getMaxWithdrawalPerDay())) {
            dto.setMaxWithdrawalPerDay(model.getMaxWithdrawalPerDay().toPlainString());
        }
        if(Objects.nonNull(model.getMaxPersWithdrawalPerDay())) {
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
        //TODO SCMNEW-4: Resolve by dariush
//        dto.setMembership(MembershipMapper.INSTANCE.toDto(model.getMembership()));
        return dto;
    }
}
