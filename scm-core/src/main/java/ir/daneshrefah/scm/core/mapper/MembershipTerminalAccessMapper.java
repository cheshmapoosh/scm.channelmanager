package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.common.data.entity.person.GeneralPersonEntity;
import ir.daneshrefah.scm.common.data.entity.terminal.TerminalEntity;
import ir.daneshrefah.scm.common.data.mapper.PersonMapper;
import ir.daneshrefah.scm.common.data.mapper.TerminalMapper;
import ir.daneshrefah.scm.common.model.asset.Membership;
import ir.daneshrefah.scm.common.model.asset.MembershipTerminalAccess;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.core.entity.asset.MembershipEntity;
import ir.daneshrefah.scm.core.entity.asset.MembershipTerminalAccessEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-25
 */
@Mapper
public interface MembershipTerminalAccessMapper {

    MembershipTerminalAccessMapper INSTANCE = Mappers.getMapper(MembershipTerminalAccessMapper.class);

    @Mapping(source = "terminal", target = "terminal", qualifiedByName = "toTerminal")
    @Mapping(source = "membership", target = "membership", qualifiedByName = "toMembership")
    MembershipTerminalAccess toMembershipTerminalAccess(MembershipTerminalAccessEntity entity);

    List<MembershipTerminalAccess> toMembershipTerminalAccessList(Iterable<MembershipTerminalAccessEntity> entity);

    @Named("toMembership")
    @Mapping(source = "person", target = "person", qualifiedByName = "toPerson")
    Membership toMembership(MembershipEntity entity);

    @Named("toPerson")
    default GeneralPerson toPerson(GeneralPersonEntity entity) {
        return PersonMapper.INSTANCE.toPerson(entity);
    }

    @Named("toTerminal")
    default Terminal toTerminal(TerminalEntity entity) {
        // Delegate the mapping to the method in ServiceMapper
        return TerminalMapper.INSTANCE.toModel(entity);
    }
}
