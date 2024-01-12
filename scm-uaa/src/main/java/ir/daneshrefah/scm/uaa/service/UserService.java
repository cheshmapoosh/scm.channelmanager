package ir.daneshrefah.scm.uaa.service;

import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.mapper.UserMapper;
import ir.daneshrefah.scm.uaa.repository.authentication.RoleEntity;
import ir.daneshrefah.scm.uaa.repository.authentication.RoleRepository;
import ir.daneshrefah.scm.uaa.repository.authentication.UserEntity;
import ir.daneshrefah.scm.uaa.repository.authentication.UserRepository;
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
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final IntegrationService integrationService;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, IntegrationService integrationService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.integrationService = integrationService;
    }

    public Optional<User> loadUserByUsername(String username, String terminalCode) {
        Integer terminalId = integrationService.findChannelIdByTerminalCode(terminalCode);
        Iterable<UserEntity> userEntities = userRepository.findByNicknameAndTerminalId(username, terminalId);
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

}
