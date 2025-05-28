package ir.daneshrefah.scm.common.data.mapper;

import ir.daneshrefah.scm.common.data.entity.asset.MembershipTerminalAccessEntity;
import ir.daneshrefah.scm.common.dto.asset.MembershipTerminalAccessDto;
import ir.daneshrefah.scm.common.model.asset.MembershipTerminalAccess;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;
import java.util.Objects;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-25
 */
@Mapper(uses = {ChannelMapper.class,MembershipMapper.class, PersonMapper.class}, componentModel = SPRING)
public interface MembershipTerminalAccessMapper {

    MembershipTerminalAccess toMembershipTerminalAccess(MembershipTerminalAccessEntity entity);

    List<MembershipTerminalAccess> toMembershipTerminalAccessList(Iterable<MembershipTerminalAccessEntity> entity);

}
