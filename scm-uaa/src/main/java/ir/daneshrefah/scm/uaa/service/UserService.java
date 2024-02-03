package ir.daneshrefah.scm.uaa.service;

import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.mapper.UserMapper;
import ir.daneshrefah.scm.uaa.repository.activation.UserActivationEntity;
import ir.daneshrefah.scm.uaa.repository.activation.UserActivationRepository;
import ir.daneshrefah.scm.uaa.repository.authentication.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-18
 */
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserActivationRepository userActivationRepository;
    private final RoleRepository roleRepository;
    private final IntegrationService integrationService;

    public Optional<User> loadUserByUsername(String username, String terminalCode) {
        Integer channelId = integrationService.findChannelIdByTerminalCode(terminalCode);
        Iterable<UserEntity> userEntities = userRepository.findByNicknameAndTerminalId(username, channelId);
        if (!userEntities.iterator().hasNext()) {
            return Optional.empty();
        }

        User user = UserMapper.INSTANCE.toModel(userEntities.iterator().next());
        user.setTerminalCode(terminalCode);

        return Optional.of(user);
    }

    public Optional<List<String>> loadUserAuthorities(Long personId) {
        List<RoleEntity> roles = roleRepository.findByPersonId(personId);
        if (null == roles || roles.isEmpty())
            return Optional.empty();
        return Optional.of(roles.stream()
                .map(r -> r.getCode())
                .collect(Collectors.toList()));
    }

    public void checkUserActivationCode(String username, String accessParameter, String activationCode) {
        List<UserActivationEntity> activationEntities = userActivationRepository.findAllByUsernameAndAccessParameterAndActivationCodeAndActivatedTrue(username,
                accessParameter, activationCode);
        return;
    }
}
