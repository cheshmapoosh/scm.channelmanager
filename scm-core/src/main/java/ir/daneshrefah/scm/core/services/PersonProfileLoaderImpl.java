package ir.daneshrefah.scm.core.services;

import ir.daneshrefah.scm.common.data.service.person.PersonService;
import ir.daneshrefah.scm.common.model.asset.MembershipTerminalAccess;
import ir.daneshrefah.scm.common.model.customer.ServiceAccess;
import ir.daneshrefah.scm.common.model.customer.UserProfile;
import ir.daneshrefah.scm.common.model.gateway.Channel;
import ir.daneshrefah.scm.common.model.message.Authentication;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.common.service.PersonProfileLoader;
import ir.daneshrefah.scm.common.service.channel.ChannelService;
import ir.daneshrefah.scm.plugin.api.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-21
 */
@RequiredArgsConstructor
@Component
public class PersonProfileLoaderImpl implements PersonProfileLoader {

    private final PersonService personService;
    private final CustomerService customerService;
    private final ChannelService channelService;
    private final ServiceAccessService serviceAccessService;

    @Override
    public UserProfile preparePersonProfile(Authentication authentication) {
        if (Objects.isNull(authentication) || !authentication.isFullyAuthenticated()) {
            return null;
        }
        if (authentication.getProfile().isPersonInfoLoaded()) {
            return authentication.getProfile();
        }
        String terminalCode = authentication.getTerminalCode();
        String nickname = authentication.getProfile().getNickname();
        GeneralPerson person = personService.findPersonByNicknameAndTerminalCode(nickname, terminalCode);
        if (Objects.isNull(person)) {
            return null;
        }
        authentication.getProfile().loadPersonInfo(person.getUsername(), person.getId());
        return authentication.getProfile();
    }

    @Override
    public UserProfile preparePersonProfileMemberships(UserProfile profile, String terminalCode) {
        if (Objects.isNull(profile)) {
            return null;
        }
        if (profile.isMembershipLoaded()) {
            return profile;
        }
        Channel channel = channelService.findChannelByCode(terminalCode).get();
        List<MembershipTerminalAccess> memberships = customerService.findMembershipChannelAccessList(
                profile.getPersonId(), channel.getId());
        profile.loadMembership(memberships);
        return profile;
    }

    @Override
    public UserProfile preparePersonProfileMemberships(Authentication authentication) {
        UserProfile profile = preparePersonProfile(authentication);
        return preparePersonProfileMemberships(profile, authentication.getTerminalCode());
    }

    @Override
    public UserProfile fillServiceAccessForProfile(UserProfile profile, String terminalCode) {
        /*
         * Controls if the 'service access' data has already been loaded, does not reload.
         * */
        if (Objects.nonNull(profile.getServiceAccesses())) {
            return profile;
        }
        List<ServiceAccess> serviceAccesses = serviceAccessService.findByPersonUsername(profile.getPersonUsername());
        profile.setServiceAccesses(serviceAccesses);
        return profile;
    }


}
