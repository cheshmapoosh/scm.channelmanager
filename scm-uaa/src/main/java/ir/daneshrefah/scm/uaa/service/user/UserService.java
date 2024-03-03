package ir.daneshrefah.scm.uaa.service.user;

import ir.daneshrefah.scm.common.data.entity.person.GeneralPersonEntity;
import ir.daneshrefah.scm.common.data.repository.PersonRepository;
import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.common.exception.*;
import ir.daneshrefah.scm.common.service.TerminalService;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.common.type.AuthenticationMethod;
import ir.daneshrefah.scm.uaa.controller.user.UserDataRequest;
import ir.daneshrefah.scm.uaa.mapper.UserMapper;
import ir.daneshrefah.scm.uaa.repository.activation.UserActivationEntity;
import ir.daneshrefah.scm.uaa.repository.activation.UserActivationRepository;
import ir.daneshrefah.scm.uaa.repository.authentication.*;
import ir.daneshrefah.scm.uaa.security.CustomMD5Encoder;
import ir.daneshrefah.scm.uaa.service.IntegrationService;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    private final CustomMD5Encoder passwordEncoder;
    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final UserActivationRepository userActivationRepository;
    private final RoleRepository roleRepository;
    private final IntegrationService integrationService;
    private final TerminalService terminalService;

    public boolean activateUser(Long userId, boolean active) {
        if (null == userId) {
            throw new MissingRequiredInputException("userId");
        }
        Optional<UserEntity> entity = userRepository.findById(userId);
        if (entity.isEmpty()) {
            throw new InvalidInputException("userId");
        }
        UserEntity userEntity = entity.get();
        userEntity.setActive(active);
        userRepository.save(entity.get());
        return true;
    }

    public User createUser(UserDataRequest request) {
        if (StringUtils.isEmpty(request.getNickname())) {
            throw new MissingRequiredInputException("nickname");
        }
        if (StringUtils.isEmpty(request.getTerminalCode())) {
            throw new MissingRequiredInputException("terminalCode");
        }
//        Optional<Terminal> terminal = terminalService.findTerminalByCode(request.getTerminalCode());
        Integer terminalId = integrationService.findChannelIdByTerminalCode(request.getTerminalCode());
        if (null == terminalId) {
            throw new InvalidInputException("terminalCode");
        }
        if (null == request.getLoginAuthenticationMethod()) {
            throw new InvalidInputException("loginAuthenticationMethod");
        }
        if (null == request.getTransactionAuthenticationMethod()) {
            throw new InvalidInputException("transactionAuthenticationMethod");
        }
        if (AuthenticationMethod.STATIC_PASSWORD.equals(request.getLoginAuthenticationMethod()) &&
                StringUtils.isEmpty(request.getLoginStaticPassword())) {
            throw new MissingRequiredInputException("loginStaticPassword");
        }
        if (AuthenticationMethod.STATIC_PASSWORD.equals(request.getTransactionAuthenticationMethod()) &&
                StringUtils.isEmpty(request.getTransactionStaticPassword())) {
            throw new MissingRequiredInputException("transactionStaticPassword");
        }
        if (StringUtils.isEmpty(request.getCreatorBranch())) {
            throw new MissingRequiredInputException("creatorBranch");
        }
        if (null == request.getPersonId()) {
            throw new MissingRequiredInputException("personId");
        }
        GeneralPersonEntity personEntity = findPersonById(request.getPersonId().intValue());
        if (null != request.getPersonId() && null == personEntity) {
            throw new InvalidInputException("personId");
        }

        GeneralPersonEntity creatorEntity = findPersonByUsername(AuthenticationUtils.getLoggedInGlobalUsername());
        UserEntity entity = new UserEntity();
        entity.setNickname(request.getNickname());
        entity.setTerminalId(terminalId);
        entity.setLoginAuthenticationMethod(request.getLoginAuthenticationMethod());
        entity.setTransactionAuthenticationMethod(request.getTransactionAuthenticationMethod());
        entity.setAccessParameters(request.getAccessParameters());
        entity.setActive(null != request.getActive() ? request.getActive() : false);
        entity.setLoginStaticPassword(passwordEncoder.encodePassword(request.getLoginStaticPassword(), personEntity.getUsername()));
        entity.setTransactionStaticPassword(passwordEncoder.encodePassword(request.getTransactionStaticPassword(), personEntity.getUsername()));
        entity.setOtpSerialNumber(request.getOtpSerialNumber());
        entity.setPerson(personEntity);
        entity.setCreatorBranch(request.getCreatorBranch());
        entity.setCreator(creatorEntity.getId());
        entity.setLastEditor(creatorEntity.getId());
        entity = userRepository.save(entity);
        return UserMapper.INSTANCE.toModel(entity);
    }

    public PagedResponseData<User> findPagedUserList(UserFindRequest request) {
        if (null == request) {
            request = new UserFindRequest();
        }
        Pageable pageable = PageRequest.of(Math.max(request.getPageNo() - 1, 0), request.getPageSize());
        Page<UserEntity> entities = userRepository.findAll(UserSpecs.toSpecification(request), pageable);
        return new PagedResponseData<>(request.getPageNo(), request.getPageSize(), entities.getTotalElements(),
                UserMapper.INSTANCE.toModels(entities.getContent()));
    }

    public Optional<User> loadUserByUsername(String username, String terminalCode) {
        Integer channelId = integrationService.findChannelIdByTerminalCode(terminalCode);
        Iterable<UserEntity> userEntities = userRepository.findByNicknameAndTerminalId(username, channelId);
        if (!userEntities.iterator().hasNext()) {
            return Optional.empty();
        }

        User user = UserMapper.INSTANCE.toModel(userEntities.iterator().next());

        return Optional.of(user);
    }

    private GeneralPersonEntity findPersonByUsername(String username) {
        List<GeneralPersonEntity> persons = personRepository.findPersonByUsername(username);
        return null != persons && persons.size() > 0 ? persons.get(0) : null;
    }

    private GeneralPersonEntity findPersonById(Integer id) {
        Optional<GeneralPersonEntity> person = personRepository.findById(id);
        return person.isPresent() ? person.get() : null;
    }

    public Optional<List<String>> loadUserAuthorities(Long personId) {
        List<RoleEntity> roles = roleRepository.findByPersonId(personId);
        if (null == roles || roles.isEmpty())
            return Optional.empty();
        return Optional.of(roles.stream()
                .map(r -> r.getCode())
                .collect(Collectors.toList()));
    }

    public boolean checkUserActivationCode(String terminalCode, String username, String accessParameter, String activationCode) {
        List<UserActivationEntity> activationEntities = userActivationRepository.findAllByTerminalCodeAndUsernameAndAccessParameterAndActivationCodeAndActivatedTrue(
                terminalCode, username, accessParameter, activationCode);
        return null != activationEntities && activationEntities.size() > 0;
    }

    public boolean updateUserLoginPassword(Long userId, UpdatePasswordRequest request) {
        if (null == request) {
            throw new MissingRequestException();
        }
        if (null == userId) {
            throw new MissingRequiredInputException("userId");
        }
        if (StringUtils.isEmpty(request.getPassword())) {
            throw new MissingRequiredInputException("password");
        }
        if (StringUtils.isEmpty(request.getPasswordConfirm())) {
            throw new MissingRequiredInputException("passwordConfirm");
        }
        if (!StringUtils.equals(request.getPassword(), request.getPasswordConfirm())) {
            throw new InvalidInputException("passwordConfirm");
        }
        Optional<UserEntity> userEntity = userRepository.findById(userId);
        if (userEntity.isEmpty()) {
            throw new InvalidInputException("userId");
        }
        userEntity.get().setLoginStaticPassword(passwordEncoder.encodePassword(request.getPassword(),
                userEntity.get().getPerson().getUsername()));
        userRepository.save(userEntity.get());
        return true;
    }

    public User updateUserTransactionStaticPass(Long userId, String password) {
        Optional<UserEntity> userEntity = userRepository.findById(userId);
        if (userEntity.isPresent()) {
            userEntity.get().setTransactionStaticPassword(password);
            return UserMapper.INSTANCE.toModel(userEntity.get());
        } else {
            throw new NoMatchRecordFoundException("User Not Found With This Id '" + userId + "'");
        }
    }

    public void deleteUserByUserId(Long userId) {
        Optional<UserEntity> userEntity = userRepository.findById(userId);
        if (userEntity.isPresent()) {
            userRepository.deleteById(userId);
        } else {
            throw new NoMatchRecordFoundException("User Not Found With This Id '" + userId + "'");
        }
    }
}
