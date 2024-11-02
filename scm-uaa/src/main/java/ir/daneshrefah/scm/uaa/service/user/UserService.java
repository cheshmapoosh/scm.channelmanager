package ir.daneshrefah.scm.uaa.service.user;

import ir.daneshrefah.scm.common.constant.SecurityConstants;
import ir.daneshrefah.scm.common.constant.otp.OtpReason;
import ir.daneshrefah.scm.common.constant.otp.OtpType;
import ir.daneshrefah.scm.common.data.entity.person.GeneralPersonEntity;
import ir.daneshrefah.scm.common.data.entity.person.GeneralRealPersonEntity;
import ir.daneshrefah.scm.common.data.entity.person.IndividualPersonEntity;
import ir.daneshrefah.scm.common.data.repository.PersonRepository;
import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.common.exception.*;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.common.model.person.PersonStatus;
import ir.daneshrefah.scm.common.model.person.UnknownPerson;
import ir.daneshrefah.scm.common.model.person.UserStatus;
import ir.daneshrefah.scm.common.model.recipient.Recipient;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.model.user.AuthenticationMethod;
import ir.daneshrefah.scm.common.model.user.UserIdentifierType;
import ir.daneshrefah.scm.common.model.user.UserType;
import ir.daneshrefah.scm.common.service.terminal.TerminalService;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.uaa.controller.user.*;
import ir.daneshrefah.scm.uaa.domain.otp.OtpAuthenticationType;
import ir.daneshrefah.scm.uaa.mapper.UserMapper;
import ir.daneshrefah.scm.uaa.repository.activation.UserActivationEntity;
import ir.daneshrefah.scm.uaa.repository.activation.UserActivationRepository;
import ir.daneshrefah.scm.uaa.repository.authentication.*;
import ir.daneshrefah.scm.uaa.security.CustomMD5Encoder;
import ir.daneshrefah.scm.uaa.security.userDetails.UserCache;
import ir.daneshrefah.scm.uaa.service.otp.OtpService;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpVerifyRequest;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpVerifyResponse;
import ir.daneshrefah.scm.utils.data.DynamicUpdateUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_ACCESS_DENIED;
import static ir.daneshrefah.scm.utils.constant.Constants.SCM_PARAMETER_AUTHORIZATION;
import static ir.daneshrefah.scm.utils.constant.Constants.SCM_PARAMETER_CLAIM_CODE;

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

    public static final String DELETE_FROM_X_USER = "DELETE FROM REF.XUSER_DETAIL WHERE USERNAME = ? AND CHANNEL_CODE = ?";

    private final UserActivationRepository userActivationRepository;
    private final PersonRepository personRepository;
    private final CustomMD5Encoder passwordEncoder;
    private final TerminalService terminalService;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final OtpService otpService;
    private final UserCache userCache;
    @PersistenceContext
    private final EntityManager entityManager;

    public User changeNickName(UserNickNameModifyRequest request, HttpServletRequest servletRequest) {
        validateUserNickNameRequest(request, servletRequest);
        UserEntity userEntity = findAuthenticatedUserByUsernameAndTerminalCode(request.getCurrentNickName(), request.getTerminalCode());
        Optional<UserEntity> foundNickNameAndTerminal = loadUserEntityByUsername(request.getNickName(), request.getTerminalCode());
        UserAuthentication currentAuthentication = AuthenticationUtils.getLoggedInUserAuthentication();
        ValidationUtils.checkNull(currentAuthentication, AuthenticationRequiredException::new);
        if (foundNickNameAndTerminal.isPresent()) {
            throw new DuplicatedRecordFoundException("username");
        }
        String claimCodeHeader = servletRequest.getHeader(SCM_PARAMETER_CLAIM_CODE);
        AuthenticationMethod currentTxAuthMethod = userEntity.getTransactionAuthenticationMethod();
        if (currentTxAuthMethod.equals(AuthenticationMethod.STATIC_PASSWORD)) {
            checkStaticPassword(userEntity, claimCodeHeader);
        } else {
            verifyOtpCode(currentAuthentication, claimCodeHeader, OtpReason.AUTHENTICATION);
        }
        userEntity.setNickname(request.getNickName());
        userEntity.setLastEditDate(LocalDateTime.now());
        userRepository.save(userEntity);
        userCache.removeUserFromCache(request.getCurrentNickName() + "::" + request.getTerminalCode());
        return UserMapper.INSTANCE.toModel(userEntity);
    }

    public UserEntity findAuthenticatedUserByUsernameAndTerminalCode(String username, String terminalCode) {
        String loggedInNickname = AuthenticationUtils.getLoggedInUserAuthentication().getName();
        String loggedInTerminalCode = Objects.requireNonNull(AuthenticationUtils.getLoggedInUser()).getTerminalCode();
        if ((!loggedInTerminalCode.equals(terminalCode) && !hasAdministratorAccess())
                || (!username.equals(loggedInNickname) && !hasAdministratorAccess())) {
            throw new AccessDeniedException(SCM_PARAMETER_AUTHORIZATION, ERROR_CODE_ACCESS_DENIED, "user does not access.");
        }
        Terminal terminal = findTerminalByCode(terminalCode);
        return loadUserEntityByUsername(username, terminal.getCode()).orElseThrow(() -> new NoMatchRecordFoundException("user"));
    }

    public Terminal findTerminalByCode(String terminalCode) {
        return terminalService.findTerminalByCode(terminalCode.toUpperCase()).orElseThrow(() -> new InvalidInputException("terminalCode"));
    }

    public User updateUserLoginStaticPassword(PasswordModificationRequest request) {
        validatePasswordModificationRequest(request);
        UserEntity userEntity = findAuthenticatedUserByUsernameAndTerminalCode(request.getUsername(), request.getTerminalCode());
        if (!userEntity.getLoginStaticPassword().equals(passwordEncoder.encodePassword(request.getOldPassword(), userEntity.getPerson().getUsername()))) {
            throw new InvalidInputException("oldPassword");
        }
//        UserEntity userEntity = userRepository.findById(user.getId()).orElseThrow(() -> new NoMatchRecordFoundException("user"));
        userEntity.setLoginStaticPassword(passwordEncoder.encodePassword(request.getNewPassword(), userEntity.getPerson().getUsername()));
        userEntity.setLastEditDate(LocalDateTime.now());
        userRepository.save(userEntity);
        removeXUser(userEntity);
        userCache.removeUserFromCache(request.getUsername() + "::" + request.getTerminalCode());
        return UserMapper.INSTANCE.toModel(userEntity);
    }

    public User updateUserTransactionStaticPass(PasswordModificationRequest request) {
        validatePasswordModificationRequest(request);
        UserEntity userEntity = findAuthenticatedUserByUsernameAndTerminalCode(request.getUsername(), request.getTerminalCode());
        if (!userEntity.getTransactionStaticPassword().equals(passwordEncoder.encodePassword(request.getOldPassword(),
                userEntity.getPerson().getUsername()))) {
            throw new InvalidInputException("oldPassword");
        }
        //attaching the record
//        UserEntity userEntity = userRepository.findById(user.getId()).orElseThrow(() -> new NoMatchRecordFoundException("user"));
        userEntity.setTransactionStaticPassword(passwordEncoder.encodePassword(request.getNewPassword(), userEntity.getPerson().getUsername()));
        userEntity.setLastEditDate(LocalDateTime.now());
        userRepository.save(userEntity);
        removeXUser(userEntity);
        userCache.removeUserFromCache(request.getUsername() + "::" + request.getTerminalCode());
        return UserMapper.INSTANCE.toModel(userEntity);
    }

    private void validatePasswordModificationRequest(PasswordModificationRequest request) {
        ValidationUtils.checkNull(request, () -> new MissingRequiredInputException("request body"));
        String username = request.getUsername();
        String oldPassword = request.getOldPassword();
        String newPassword = request.getNewPassword();
        String terminalCode = request.getTerminalCode();
        UserAuthentication loggedInUserAuthentication = AuthenticationUtils.getLoggedInUserAuthentication();
        if (Objects.isNull(loggedInUserAuthentication) || StringUtils.isBlank(loggedInUserAuthentication.getName())) {
            throw new AuthenticationRequiredException();
        }
        ValidationUtils.checkBlankString(username, () -> new InvalidInputException("username"));
        ValidationUtils.checkBlankString(terminalCode, () -> new InvalidInputException("terminalCode"));
        ValidationUtils.checkBlankString(oldPassword, () -> new InvalidInputException("oldPassword"));
        ValidationUtils.checkBlankString(newPassword, () -> new InvalidInputException("newPassword"));
        User loggedInUser = AuthenticationUtils.getLoggedInUser();
        ValidationUtils.checkNull(loggedInUser, AuthenticationRequiredException::new);
        assert loggedInUser != null;
        ValidationUtils.checkNull(loggedInUser.getTerminalCode(), () -> new MissingRequiredInputException("X-SCM-Terminal"));
        if (oldPassword.equals(newPassword)) {
            throw new InvalidInputException("newPassword");
        }
        if (newPassword.length() < 8
            || StringUtils.isNumeric(newPassword)
            || !StringUtils.isAlphanumeric(newPassword)) {
            throw new InvalidInputException("security constraints");
        }
    }

    private boolean hasAdministratorAccess() {
        UserAuthentication loggedInUserAuthentication = AuthenticationUtils.getLoggedInUserAuthentication();
        ValidationUtils.checkNull(loggedInUserAuthentication, AuthenticationRequiredException::new);
        assert loggedInUserAuthentication != null;
        return loggedInUserAuthentication.hasAuthority(SecurityConstants.ROLE_ADMIN_USER);
    }

    private void validateUserNickNameRequest(UserNickNameModifyRequest request, HttpServletRequest servletRequest) {
        String claimCodeHeader = servletRequest.getHeader(SCM_PARAMETER_CLAIM_CODE);
        ValidationUtils.checkBlankString(claimCodeHeader, () -> new MissingRequiredInputException(SCM_PARAMETER_CLAIM_CODE));
        ValidationUtils.checkNull(request, () -> new MissingRequiredInputException("request body"));
        ValidationUtils.checkBlankString(request.getNickName(), () -> new MissingRequiredInputException("nickname"));
        ValidationUtils.checkBlankString(request.getCurrentNickName(), () -> new MissingRequiredInputException("currentNickname"));
        ValidationUtils.checkBlankString(request.getTerminalCode(), () -> new MissingRequiredInputException("terminalCode"));
        ValidationUtils.checkEqualsIgnoreCaseString(request.getNickName(), request.getCurrentNickName(), () -> new InvalidInputException("nickname"));
        UserAuthentication loggedInUserAuthentication = AuthenticationUtils.getLoggedInUserAuthentication();
        if (Objects.isNull(loggedInUserAuthentication) || StringUtils.isBlank(loggedInUserAuthentication.getName())) {
            throw new AuthenticationRequiredException();
        }
        User loggedInUser = AuthenticationUtils.getLoggedInUser();
        ValidationUtils.checkNull(loggedInUser, AuthenticationRequiredException::new);
    }

    public boolean activateUser(Integer userId, boolean active) {
        if (null == userId) {
            throw new MissingRequiredInputException("userId");
        }
        Optional<UserEntity> entity = userRepository.findById(userId);
        if (entity.isEmpty()) {
            throw new InvalidInputException("userId");
        }
        UserEntity userEntity = entity.get();
        userEntity.setStatus(UserStatus.ACTIVE);
        userRepository.save(entity.get());
        return true;
    }

    public User createShahkarVerifiedUserAndDeleteOld(String nationalCode, String mobileNo, String terminalCode) {
        ValidationUtils.checkBlankString(nationalCode, () -> new MissingRequiredInputException("nationalCode"));
        ValidationUtils.checkInvalidMobileNumber(mobileNo, () -> new InvalidInputException("mobileNo"));
        ValidationUtils.checkBlankString(terminalCode, () -> new MissingRequiredInputException("terminalCode"));
        Optional<Terminal> terminal = terminalService.findTerminalByCode(terminalCode);
        ValidationUtils.checkEmptyOptional(terminal, () -> new InvalidInputException("terminalCode"));

        //TODO verify shahkar

        GeneralRealPersonEntity personEntity = personRepository.findRealPersonByNationalCode(nationalCode);
        if (Objects.isNull(personEntity)) {
            personEntity = new IndividualPersonEntity();
            personEntity.setUsername(nationalCode);
//            personEntity.setPersonType(PersonType.REAL);
//            personEntity.setNationality();
//            personEntity.setRegisterIssueDate();
            personEntity.setStatus(PersonStatus.ACTIVE);
//            personEntity.setBranchCode();
//            personEntity.setPhone1();
//            personEntity.setPhone2();
//            personEntity.setMobile1();
//            personEntity.setMobile2();
//            personEntity.setMobile3();
//            personEntity.setEmail();
//            personEntity.setFax();
//            personEntity.setAddress1();
//            personEntity.setAddress2();
//            personEntity.setAddress3();
//            personEntity.setAddress4();
//            personEntity.setPostalCode1();
//            personEntity.setPostalCode2();
//            personEntity.setShahabCode();
//            personEntity.setArchiveNo();
//            personEntity.setFirstName();
//            personEntity.setFirstNameEnglish();
//            personEntity.setLastName();
//            personEntity.setLastNameEnglish();
//            personEntity.setFatherName();
            personEntity.setNationalCode(nationalCode);
//            personEntity.setIdentificationNo();
//            personEntity.setIdentificationSeries();
//            personEntity.setIdentificationSerial();
//            personEntity.setIdentificationDocumentTypeCode();
//            personEntity.setMaritalStatus();
//            personEntity.setJobCode();
//            personEntity.setEducationCode();
//            personEntity.setMajorCode();
//            personEntity.setGender();
//            personEntity.setBirthDate();
//            personEntity.setDeadDate();
            personEntity.setLived(true);
        }
        Optional<List<UserEntity>> userEntities = Optional.empty();
        if (Objects.nonNull(personEntity.getId())) {
            userEntities = Optional.of(userRepository.findByPersonIdAndLegacyTerminalId(personEntity.getId(),
                    terminal.get().getLegacyTerminalId().intValue()));
        }

        return null;
    }

    public User createSmsVerifiedUserAndDeleteOld(String mobileNo, String terminalCode) {
        ValidationUtils.checkBlankString(terminalCode, () -> new MissingRequiredInputException("terminalCode"));
        Optional<Terminal> terminal = terminalService.findTerminalByCode(terminalCode);
//        ValidationUtils.checkIsNotNullAndIsNotEmptyOptional(terminal, () -> new InvalidInputException("terminalCode"));
//TODO user must be create and store on database with person info,
// also old user with same username must be update or set status to deleted
        Optional<UserEntity> currentUserEntity = loadUserEntityByUsername(mobileNo, terminalCode);
        if (currentUserEntity.isPresent()) {
//TODO            ValidationUtils.checkNotEqualsObject(UserType.SMS_VERIFIED, currentUserEntity.get().getType(), () -> null);
//            if user with same mobileNo exist and type of it isn't SMS_VERIFIED, we can throw exception or
//             generate random nickname for new user
            currentUserEntity.get().setStatus(UserStatus.DELETED);
            currentUserEntity.get().getPerson().setStatus(PersonStatus.DELETED);
        }

        UserEntity entity = new UserEntity();
        entity.setType(UserType.SMS_VERIFIED);
        entity.setNickname(mobileNo);
        entity.setTerminalId(terminal.get().getLegacyTerminalId().intValue());
        /*
            this code include in token and in refresh time, will be checked.
        */
        entity.setLoginStaticPassword(StringUtils.randomAlphanumeric(10));
//        entity.setLoginAuthenticationMethod(request.getLoginAuthenticationMethod());
//        entity.setTransactionAuthenticationMethod(request.getTransactionAuthenticationMethod());
//        entity.setAccessParameters(validateAccessParameter(request.getAccessParameters()));
        entity.setStatus(UserStatus.ACTIVE);
//        entity.setLoginStaticPassword(passwordEncoder.encodePassword(request.getLoginStaticPassword(), personEntity.getUsername()));
//        entity.setTransactionStaticPassword(passwordEncoder.encodePassword(request.getTransactionStaticPassword(), personEntity.getUsername()));
//        entity.setOtpSerialNumber(request.getOtpSerialNumber());
//        entity.setPerson(personEntity);
//        entity.setCreatorBranch(request.getCreatorBranch());
//        entity.setCreator(creatorEntity.getId());
//        entity.setLastEditor(creatorEntity.getId());
        User user = UserMapper.INSTANCE.toModel(entity);

        GeneralPerson person = new UnknownPerson();
        person.setId(new Random().nextInt());
        person.setUsername(StringUtils.generateGuid());
        person.setStatus(PersonStatus.ACTIVE);
        person.setMobile1(mobileNo);
        user.setPerson(person);

        return user;
    }

    public User createUser(UserDataRequest request) {
        ValidationUtils.checkBlankString(request.getNickname(), () -> new MissingRequiredInputException("nickname"));
        ValidationUtils.checkBlankString(request.getTerminalCode(), () -> new MissingRequiredInputException("terminalCode"));
        ValidationUtils.checkBlankString(request.getCreatorBranch(), () -> new MissingRequiredInputException("creatorBranch"));
        ValidationUtils.checkNull(request.getPersonId(), () -> new MissingRequiredInputException("personId"));
        ValidationUtils.checkNull(request.getLoginAuthenticationMethod(), () -> new InvalidInputException("loginAuthenticationMethod"));
        ValidationUtils.checkNull(request.getTransactionAuthenticationMethod(), () -> new InvalidInputException("transactionAuthenticationMethod"));
        Optional<Terminal> terminal = terminalService.findTerminalByCode(request.getTerminalCode());
        ValidationUtils.checkEmptyOptional(terminal, () -> new InvalidInputException("terminalCode"));

        if (AuthenticationMethod.STATIC_PASSWORD.equals(request.getLoginAuthenticationMethod()) &&
            StringUtils.isEmpty(request.getLoginStaticPassword())) {
            throw new MissingRequiredInputException("loginStaticPassword");
        }
        if (AuthenticationMethod.STATIC_PASSWORD.equals(request.getTransactionAuthenticationMethod()) &&
            StringUtils.isEmpty(request.getTransactionStaticPassword())) {
            throw new MissingRequiredInputException("transactionStaticPassword");
        }
        GeneralPersonEntity personEntity = findPersonById(request.getPersonId().intValue());
        ValidationUtils.checkNull(personEntity, () -> new InvalidInputException("personId"));
        GeneralPersonEntity creatorEntity = findPersonByUsername(AuthenticationUtils.getLoggedInGlobalUsername());
        ValidationUtils.checkNull(creatorEntity, () -> new NoMatchRecordFoundException("creator person does not login"));
        assert creatorEntity != null;
        UserEntity entity = mapToUserEntity(request, terminal.get().getLegacyTerminalId().intValue(), creatorEntity, personEntity);
        entity = userRepository.save(entity);
        return UserMapper.INSTANCE.toModel(entity);
    }

    private UserEntity mapToUserEntity(UserDataRequest request, Integer terminalId, GeneralPersonEntity creatorEntity, GeneralPersonEntity personEntity) {
        UserEntity entity = new UserEntity();
        entity.setNickname(request.getNickname());
        entity.setTerminalId(terminalId);
        entity.setLoginAuthenticationMethod(request.getLoginAuthenticationMethod());
        entity.setTransactionAuthenticationMethod(request.getTransactionAuthenticationMethod());
        entity.setAccessParameters(validateAccessParameter(request.getAccessParameters()));
        entity.setStatus(UserStatus.ACTIVE);
        entity.setLoginStaticPassword(passwordEncoder.encodePassword(request.getLoginStaticPassword(), personEntity.getUsername()));
        entity.setTransactionStaticPassword(passwordEncoder.encodePassword(request.getTransactionStaticPassword(), personEntity.getUsername()));
        entity.setOtpSerialNumber(request.getOtpSerialNumber());
        entity.setPerson(personEntity);
        entity.setCreatorBranch(request.getCreatorBranch());
        entity.setCreator(creatorEntity.getId());
        entity.setLastEditor(creatorEntity.getId());
        entity.setCreateDate(LocalDateTime.now());
        entity.setLastEditDate(LocalDateTime.now());
        return entity;
    }

    private Set<String> validateAccessParameter(Set<String> accessParameters) {
        if (Objects.nonNull(accessParameters) && !accessParameters.isEmpty()) {
            return accessParameters.stream().filter(param -> !(param.startsWith(";") && param.endsWith(";")))
                    .map(param -> ";" + param + ";")
                    .collect(Collectors.toSet());
        }
        return accessParameters;
    }

    public PagedResponseData<User> findPagedUserList(UserFindRequest request) {
        validateFindPagedUserList(request);
        Pageable pageable = PageRequest.of(Math.max(request.getPageNo() - 1, 0), request.getPageSize());
        Page<UserEntity> entities = userRepository.findAll(UserSpecs.toSpecification(request), pageable);
        return new PagedResponseData<>(request.getPageNo(), request.getPageSize(), entities.getTotalElements(),
                UserMapper.INSTANCE.toModels(entities.getContent()));
    }

    private void validateFindPagedUserList(UserFindRequest request) {
        if (null == request) {
            request = new UserFindRequest();
        }
        if (Objects.isNull(request.getPageNo())) {
            request.setPageNo(0);
        }
        if (Objects.isNull(request.getPageSize())) {
            request.setPageSize(10);
        }
        ValidationUtils.checkBlankStringIfNotNull(request.getNickname(), () -> new InvalidInputException("nickname"));
        ValidationUtils.checkBlankStringIfNotNull(request.getCreatorBranch(), () -> new InvalidInputException("creatorBranch"));
        ValidationUtils.checkBlankStringIfNotNull(request.getTerminalCode(), () -> new InvalidInputException("terminalCode"));
        ValidationUtils.checkBlankStringIfNotNull(String.valueOf(request.getTerminalId()), () -> new InvalidInputException("terminalId"));
        if (StringUtils.isNotEmpty(request.getTerminalCode())) {
            Terminal terminal = terminalService.findTerminalByCode(request.getTerminalCode()).orElseThrow(() -> new InvalidInputException("terminalCode"));
            request.setTerminalId(Math.toIntExact(terminal.getLegacyTerminalId()));
        }
    }

    public Optional<UserEntity> loadUserEntityByUsername(String username, String terminalCode) {
        Integer channelId = findTerminalByCode(terminalCode).getLegacyTerminalId().intValue();
//        Integer channelId = terminalService.findTerminalByCode(terminalCode).get().getLegacyTerminalId().intValue();
//        Integer channelId = integrationService.findChannelIdByTerminalCode(terminalCode);
        Iterable<UserEntity> userEntities = findByNicknameAndLegacyTerminalId(username, channelId);
        if (!userEntities.iterator().hasNext()) {
            return Optional.empty();
        }
        return Optional.of(userEntities.iterator().next());
    }

    public Optional<User> loadUserByUsername(String username, String terminalCode) {
        Optional<UserEntity> userEntity = loadUserEntityByUsername(username, terminalCode);
        if (userEntity.isEmpty()) {
            return Optional.empty();
        }

        User user = UserMapper.INSTANCE.toModel(userEntity.get());
        return Optional.of(user);
    }

    public GeneralPersonEntity findPersonByUsername(String username) {
        List<GeneralPersonEntity> persons = personRepository.findPersonByUsername(username);
        return null != persons && !persons.isEmpty() ? persons.get(0) : null;
    }

    public GeneralPersonEntity findPersonById(Integer id) {
        Optional<GeneralPersonEntity> person = personRepository.findById(id);
        return person.orElse(null);
    }

    public Optional<List<String>> loadUserAuthorities(Long personId) {
        List<RoleEntity> roles = roleRepository.findByPersonId(personId);
        if (null == roles || roles.isEmpty())
            return Optional.empty();
        return Optional.of(roles.stream()
                .map(RoleEntity::getCode)
                .collect(Collectors.toList()));
    }

    public boolean checkUserActivationCode(String terminalCode, String username, String accessParameter, String activationCode) {
        List<UserActivationEntity> activationEntities = userActivationRepository.findAllByTerminalCodeAndUsernameAndAccessParameterAndActivationCodeAndActivatedTrue(
                terminalCode, username, accessParameter, activationCode);
        return null != activationEntities && !activationEntities.isEmpty();
    }

    public void deleteUserByUserId(Integer userId, UserDeleteRequest userDeleteRequest) {
        validateUserDeleteRequest(userDeleteRequest);
        userRepository.findById(userId).ifPresentOrElse(userEntity -> {
            if (!userEntity.getLastEditDate().equals(userDeleteRequest.getLastEditDate())) {
                throw new RecordVersionException("lastEditDate");
            }
            userRepository.deleteById(userId);
        }, () -> {
            throw new NoMatchRecordFoundException("User Not Found With This Id '" + userId + "'");
        });
    }

    private void validateUserDeleteRequest(UserDeleteRequest userDeleteRequest) {
        ValidationUtils.checkNull(userDeleteRequest.getLastEditDate(), () -> new InvalidInputException("lastEditDate"));
    }


    public User updateLoginPasswordMethod(AuthenticationMethodModificationRequest request) {
        validateAuthenticationMethodModificationRequest(request);
        UserAuthentication loggedInUserAuthentication = AuthenticationUtils.getLoggedInUserAuthentication();
        ValidationUtils.checkNull(loggedInUserAuthentication, AuthenticationRequiredException::new);
        assert loggedInUserAuthentication != null;
        AuthenticationMethod requestMethod = request.getAuthenticationMethod();
        if (requestMethod.equals(AuthenticationMethod.SMS) || requestMethod.equals(AuthenticationMethod.OTP)) {
            verifyOtpCode(loggedInUserAuthentication, request.getCredential(), OtpReason.CHANGE_LOGIN_AUTHENTICATION_METHOD);
        }
        UserEntity userEntity = findUser(loggedInUserAuthentication);
        String terminalCode = loggedInUserAuthentication.getTerminalCode();
        String nickname = loggedInUserAuthentication.getName();
        userEntity.setLastEditDate(LocalDateTime.now());
        userEntity.setLoginAuthenticationMethod(requestMethod);
        userRepository.save(userEntity);
        removeXUser(userEntity);
        userCache.removeUserFromCache(nickname + "::" + terminalCode);
        return UserMapper.INSTANCE.toModel(userEntity);
    }

    public void verifyOtpCode(UserAuthentication loggedInUserAuthentication, String credential, OtpReason reason) {
        OtpVerifyRequest otpVerifyRequest = OtpVerifyRequest.builder()
                .otpType(OtpType.SMS)
                .recipient(getCurrentRecipient(loggedInUserAuthentication))
                .reason(reason)
                .claimCode(credential)
                .build();
        OtpVerifyResponse otpVerifyResponse = otpService.verifyOtp(otpVerifyRequest);
        if (!otpVerifyResponse.isSuccessful()) {
            throw new InvalidInputException("otpCode");
        }
    }

    public Recipient getCurrentRecipient(UserAuthentication loggedInUserAuthentication) {
        User principal = loggedInUserAuthentication.getPrincipal();
        GeneralPerson principalPerson = principal.getPerson();
        GeneralPersonEntity personEntity = personRepository.findById(principalPerson.getId()).orElseThrow(AuthenticationRequiredException::new);
        String terminalCode = principal.getTerminalCode();
        String accessParameter = principal.getAccessParameters().stream().map(param -> ";" + param + ";").collect(Collectors.joining(","));
        String mobileNumber = personEntity.getMobile1();
        return Recipient.builder()
                .address(mobileNumber)
                .identifier(principal.getNickname())
                .identifierType(UserIdentifierType.USER_NICKNAME)
                .terminalCode(terminalCode)
                .accessParameter(accessParameter)
                .build();
    }

    private void validateAuthenticationMethodModificationRequest(AuthenticationMethodModificationRequest request) {
        AuthenticationMethod authenticationMethod = request.getAuthenticationMethod();
        String credential = request.getCredential();
        ValidationUtils.checkNull(authenticationMethod, () -> new InvalidInputException("authenticationMethod"));
        ValidationUtils.checkBlankString(credential, () -> new InvalidInputException("credential"));
        User loggedInUser = AuthenticationUtils.getLoggedInUser();
        ValidationUtils.checkNull(loggedInUser, AuthenticationRequiredException::new);
        assert loggedInUser != null;
        ValidationUtils.checkNull(loggedInUser.getTerminalCode(), () -> new MissingRequiredInputException("X-SCM-Terminal"));
        UserAuthentication loggedInUserAuthentication = AuthenticationUtils.getLoggedInUserAuthentication();
        if (Objects.isNull(loggedInUserAuthentication) || StringUtils.isBlank(loggedInUserAuthentication.getName())) {
            throw new AuthenticationRequiredException();
        }
    }

    @Transactional
    public User updateTransactionPasswordMethod(AuthenticationMethodModificationRequest request, HttpServletRequest servletRequest) {
        validateAuthenticationMethodModificationRequest(request);
        String headerClaimCode = servletRequest.getHeader(SCM_PARAMETER_CLAIM_CODE);
        ValidationUtils.checkBlankString(headerClaimCode, () -> new MissingRequiredInputException(SCM_PARAMETER_CLAIM_CODE));
        UserAuthentication currentUserAuthentication = AuthenticationUtils.getLoggedInUserAuthentication();
        ValidationUtils.checkNull(currentUserAuthentication, AuthenticationRequiredException::new);
        assert currentUserAuthentication != null;
        UserEntity userEntity = findUser(currentUserAuthentication);
        AuthenticationMethod currentTxMethod = userEntity.getTransactionAuthenticationMethod();
        AuthenticationMethod requestMethod = request.getAuthenticationMethod();
        validateTransactionMethodChangeServiceAccess(currentTxMethod, userEntity, headerClaimCode, request, currentUserAuthentication);
        userEntity.setLastEditDate(LocalDateTime.now());
        userEntity.setTransactionAuthenticationMethod(requestMethod);
        String terminalCode = currentUserAuthentication.getTerminalCode();
        String nickname = currentUserAuthentication.getName();
        userCache.removeUserFromCache(nickname + "::" + terminalCode);
        removeXUser(userEntity);
        userRepository.save(userEntity);
        return UserMapper.INSTANCE.toModel(userEntity);
    }

    private void removeXUser(UserEntity userEntity) {
        UserAuthentication currentAuthentication = AuthenticationUtils.getLoggedInUserAuthentication();
        assert currentAuthentication != null;
        entityManager.createNativeQuery(DELETE_FROM_X_USER)
                .setParameter(1, userEntity.getNickname())
                .setParameter(2, currentAuthentication.getTerminalCode())
                .executeUpdate();
    }

    private void validateTransactionMethodChangeServiceAccess(AuthenticationMethod currentTxMethod
            , UserEntity userEntity
            , String claimCode
            , AuthenticationMethodModificationRequest request
            , UserAuthentication currentUserAuthentication) {
        if (currentTxMethod.equals(AuthenticationMethod.STATIC_PASSWORD)) {
            checkStaticPassword(userEntity, claimCode);
            verifyOtpCode(currentUserAuthentication, request.getCredential(), OtpReason.CHANGE_TRANSACTION_AUTHENTICATION_METHOD);
        } else {
            verifyOtpCode(currentUserAuthentication, claimCode, OtpReason.CHANGE_TRANSACTION_AUTHENTICATION_METHOD);
            checkStaticPassword(userEntity, request.getCredential());
        }
    }

    public void checkStaticPassword(UserEntity userEntity, String credential) {
        if (!userEntity.getTransactionStaticPassword().equals(passwordEncoder.encodePassword(credential, userEntity.getPerson().getUsername()))) {
            throw new InvalidInputException("static password");
        }
    }

    public boolean validateStaticPassword(UserEntity userEntity, String credential, OtpAuthenticationType otpAuthenticationType) {
        if (StringUtils.isBlank(userEntity.getTransactionStaticPassword()) || otpAuthenticationType == null) {
            return false;
        }
        if (otpAuthenticationType.equals(OtpAuthenticationType.TRANSACTION)) {
            return userEntity.getTransactionStaticPassword().equals(passwordEncoder.encodePassword(credential, userEntity.getPerson().getUsername()));
        } else {
            return userEntity.getLoginStaticPassword().equals(passwordEncoder.encodePassword(credential, userEntity.getPerson().getUsername()));
        }
    }

    public UserEntity findUser(UserAuthentication loggedInUserAuthentication) {
        User principal = loggedInUserAuthentication.getPrincipal();
        String terminalCode = principal.getTerminalCode();
        String nickname = loggedInUserAuthentication.getName();
        UserEntity userEntity = findAuthenticatedUserByUsernameAndTerminalCode(nickname, terminalCode);
        //attaching the record
        return userRepository.findById(userEntity.getId()).orElseThrow(() -> new NoMatchRecordFoundException("username"));
    }

    public User changeUser(UserDataChangeRequest request) {
        //TODO check admin access role
        validateUserDataChangeRequest(request);
        UserEntity userEntity = userRepository.findById(request.getId()).orElseThrow(() -> new NoMatchRecordFoundException("id"));
        applyDynamicUpdateChanges(userEntity, request);
        userEntity.setLastEditDate(LocalDateTime.now());
        if (Objects.nonNull(AuthenticationUtils.getLoggedInUserAuthentication())
            && Objects.nonNull(AuthenticationUtils.getLoggedInUserAuthentication().getPrincipal())) {
            userEntity.setLastEditor(AuthenticationUtils.getLoggedInUserAuthentication().getPrincipal().getId());
        }
        Terminal terminal = terminalService.findTerminalByLegacyId(userEntity.getTerminalId()).orElseThrow(() -> new NoMatchRecordFoundException("terminal"));
        userRepository.save(userEntity);
        userCache.removeUserFromCache(request.getNickname() + "::" + terminal.getCode());
        return UserMapper.INSTANCE.toModel(userEntity);
    }

    private void applyDynamicUpdateChanges(UserEntity userEntity, UserDataChangeRequest request) {
        DynamicUpdateUtils.applyChangesIfNotBlank(request.getNickname(), userEntity::setNickname);
        DynamicUpdateUtils.applyChangesIfNotBlank(request.getOtpSerialNumber(), userEntity::setOtpSerialNumber);
        DynamicUpdateUtils.applyChangesIfNotBlank(request.getLoginStaticPassword(), userEntity::setLoginStaticPassword);
        DynamicUpdateUtils.applyChangesIfNotBlank(request.getTransactionStaticPassword(), userEntity::setTransactionStaticPassword);
        DynamicUpdateUtils.applyChangesIfNotEmptySet(request.getAccessParameters(), accessParameters -> {
            userEntity.setAccessParameters(validateAccessParameter(accessParameters));
        });
        DynamicUpdateUtils.applyChangesIfNotNull(request.getActive(), active -> {
            userEntity.setStatus(active ? UserStatus.ACTIVE : UserStatus.INACTIVE);
        });
        DynamicUpdateUtils.applyChangesIfNotNull(request.getTransactionAuthenticationMethod(), userEntity::setTransactionAuthenticationMethod);
        DynamicUpdateUtils.applyChangesIfNotNull(request.getLoginAuthenticationMethod(), userEntity::setLoginAuthenticationMethod);
    }

    private void validateUserDataChangeRequest(UserDataChangeRequest request) {
        ValidationUtils.checkNumericInput(request.getId(), () -> new MissingRequiredInputException("id"));
        ValidationUtils.checkBlankStringIfNotNull(request.getNickname(), () -> new InvalidInputException("nickname"));
        ValidationUtils.checkBlankStringIfNotNull(request.getOtpSerialNumber(), () -> new InvalidInputException("otpSerialNumber"));
        ValidationUtils.checkBlankStringIfNotNull(request.getLoginStaticPassword(), () -> new InvalidInputException("loginStaticPassword"));
        ValidationUtils.checkBlankStringIfNotNull(request.getTransactionStaticPassword(), () -> new InvalidInputException("transactionStaticPassword"));
    }

    public User findUserById(Integer userId) {
        return UserMapper.INSTANCE.toModel(userRepository.findById(userId).orElseThrow(() -> new NoMatchRecordFoundException("userId")));
    }

    public List<UserEntity> findByPersonIdAndLegacyTerminalCode(Integer userId, String terminalCode) {
        Terminal terminal = findTerminalByCode(terminalCode);
        return userRepository.findByPersonIdAndLegacyTerminalId(userId, terminal.getLegacyTerminalId().intValue());
    }

    public User findByNicknameAndTerminalCode(String nickname, String terminalCode) {
        Integer terminalId = Integer.valueOf(terminalService.findTerminalByCode(terminalCode)
                .orElseThrow(() -> new InvalidInputException("terminalCode")).getId());
        List<UserEntity> userEntities = findByNicknameAndLegacyTerminalId(nickname, terminalId);
        if (Objects.nonNull(userEntities) && userEntities.size() > 0) {
            return UserMapper.INSTANCE.toModel(userEntities.get(0));
        }
        return null;
    }

    public List<UserEntity> findByNicknameAndLegacyTerminalId(String nickname, Integer terminalId) {
        return userRepository.findByNicknameAndLegacyTerminalId(nickname, terminalId);
    }

    public List<UserEntity> findByNicknameAndLegacyTerminalCode(String nickname, String terminalCode) {
        Terminal terminal = findTerminalByCode(terminalCode);
        return userRepository.findByNicknameAndLegacyTerminalId(nickname, terminal.getLegacyTerminalId().intValue());
    }
}
