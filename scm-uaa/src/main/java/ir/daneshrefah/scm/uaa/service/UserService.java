package ir.daneshrefah.scm.uaa.service;

import ir.daneshrefah.scm.uaa.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.uaa.common.model.person.IndividualPerson;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.mapper.UserMapper;
import ir.daneshrefah.scm.uaa.repository.authentication.UserRepository;
import ir.daneshrefah.scm.uaa.repository.authentication.UserEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

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
    private final IntegrationService integrationService;

    public UserService(UserRepository userRepository, IntegrationService integrationService) {
        this.userRepository = userRepository;
        this.integrationService = integrationService;
    }

    public Optional<User> loadUserByUsername(String username, String terminalCode) {
        Integer terminalId = integrationService.findChannelIdByTerminalCode(terminalCode);
        Iterable<UserEntity> userEntities = userRepository.findByNicknameAndTerminalId(username, terminalId);
        if (!userEntities.iterator().hasNext()) {
            return Optional.empty();
        }
        GeneralPerson person = new IndividualPerson();
        person.setId(UUID.randomUUID().toString());

        User user = UserMapper.INSTANCE.toModel(userEntities.iterator().next());
        user.setTerminalCode(terminalCode);

        return Optional.of(user);
    }

}
