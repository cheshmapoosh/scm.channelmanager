package ir.daneshrefah.scm.uaa.service.user;

import ir.daneshrefah.scm.common.constant.SecurityConstants;
import ir.daneshrefah.scm.common.constant.TerminalType;
import ir.daneshrefah.scm.common.constant.otp.OtpReason;
import ir.daneshrefah.scm.common.constant.otp.OtpType;
import ir.daneshrefah.scm.common.data.entity.person.GeneralLegalPersonEntity;
import ir.daneshrefah.scm.common.data.entity.person.GeneralPersonEntity;
import ir.daneshrefah.scm.common.data.entity.person.GeneralRealPersonEntity;
import ir.daneshrefah.scm.common.data.entity.person.IndividualPersonEntity;
import ir.daneshrefah.scm.common.data.repository.PersonRepository;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.dto.terminal.TerminalService;
import ir.daneshrefah.scm.common.exception.*;
import ir.daneshrefah.scm.common.model.message.IssuerInfo;
import ir.daneshrefah.scm.common.model.notification.NotificationData;
import ir.daneshrefah.scm.common.model.notification.NotificationRequest;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationDataKey;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationMedia;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationTemplate;
import ir.daneshrefah.scm.common.model.person.*;
import ir.daneshrefah.scm.common.model.recipient.Recipient;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.model.user.AuthenticationMethod;
import ir.daneshrefah.scm.common.model.user.UserIdentifierType;
import ir.daneshrefah.scm.common.model.user.UserType;
import ir.daneshrefah.scm.notification.client.service.spec.NotificationService;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.uaa.controller.user.*;
import ir.daneshrefah.scm.uaa.controller.user.UpdatePasswordRequest;
import ir.daneshrefah.scm.uaa.domain.otp.AuthenticationMethodType;
import ir.daneshrefah.scm.uaa.mapper.UserMapper;
import ir.daneshrefah.scm.uaa.repository.authentication.*;
import ir.daneshrefah.scm.uaa.repository.authentication.client.FindUserByNationalCodeSpecs;
import ir.daneshrefah.scm.uaa.security.password.PasswordHashService;
import ir.daneshrefah.scm.uaa.service.credential.CredentialGenerator;
import ir.daneshrefah.scm.uaa.service.otp.OtpService;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpVerifyRequest;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpVerifyResponse;
import ir.daneshrefah.scm.uaa.service.person.UPersonService;
import ir.daneshrefah.scm.utils.data.DynamicUpdateUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_ACCESS_DENIED;
import static ir.daneshrefah.scm.common.model.notification.constants.NotificationTemplate.RESET_FIRST_PASSWORD;
import static ir.daneshrefah.scm.common.model.notification.constants.NotificationTemplate.RESET_SECOND_PASSWORD;
import static ir.daneshrefah.scm.uaa.domain.otp.AuthenticationMethodType.LOGIN;
import static ir.daneshrefah.scm.uaa.utils.RequestUtils.extractRequestAccessParameter;
import static ir.daneshrefah.scm.uaa.utils.RequestUtils.extractRequestTerminalCode;
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
@Slf4j
public class UserService {
    private final static String CUSTOMER_ROLE_CODE = "ROLE_CUSTOMER";
    //    private final UserActivationRepository userActivationRepository;
    private final PersonRepository personRepository;
    private final PasswordHashService passwordHashService;
    private final TerminalService terminalService;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final OtpService otpService;
    private final UserCache userCache;
    private final XUserDetailService xUserDetailService;
    private final CredentialGenerator credentialGenerator;
    private final UPersonService uPersonService;
    private final NotificationService notificationService;
    private final UserMapper userMapper;

    private String userCacheKey(String username, String terminalCode) {
        return username + StringUtils.DOUBLE_COLON + terminalCode;
    }

    @Transactional
    public User changeNickName(UserNickNameModifyRequest request, HttpServletRequest servletRequest) {
        String claimCodeHeader = servletRequest.getHeader(SCM_PARAMETER_CLAIM_CODE);
        ValidationUtils.checkBlankString(claimCodeHeader, () -> new MissingRequiredInputException(SCM_PARAMETER_CLAIM_CODE));
        validateUserNickNameRequest(request);
        UserEntity userEntity = findAuthenticatedUserByUsernameAndTerminalCode(request.getCurrentNickName(), request.getTerminalCode());
        Optional<UserEntity> foundNickNameAndTerminal = loadUserEntityByUsername(request.getNickName(), request.getTerminalCode());
        UserAuthentication currentAuthentication = AuthenticationUtils.getLoggedInUserAuthentication();
        ValidationUtils.checkNull(currentAuthentication, AuthenticationRequiredException::new);
        if (foundNickNameAndTerminal.isPresent()) {
            throw new DuplicatedRecordFoundException("username");
        }
        AuthenticationMethod currentTxAuthMethod = userEntity.getTransactionAuthenticationMethod();
        if (currentTxAuthMethod.equals(AuthenticationMethod.STATIC_PASSWORD)) {
            checkStaticPassword(userEntity, claimCodeHeader);
        } else {
            OtpType otpType = OtpType.toOtpType(currentTxAuthMethod);
            verifyOtpCode(currentAuthentication, claimCodeHeader, OtpReason.AUTHENTICATION, otpType);
        }
        userEntity.setNickname(request.getNickName());
        userEntity.setLastEditDate(LocalDateTime.now());
        userRepository.save(userEntity);
        assert currentAuthentication != null;
        String terminalCode = currentAuthentication.getTerminalCode();
        xUserDetailService.removeXUserByUsernameAndChannelCode(userEntity, terminalCode);
        userCache.removeUserFromCache(userCacheKey(request.getCurrentNickName(), request.getTerminalCode()));
        return userMapper.toModel(userEntity);
    }

    @Transactional
    public User changeNickNameByAdmin(UserNickNameModifyRequest request) {
        validateUserNickNameRequest(request);
        loadUserEntityByUsername(request.getNickName(), request.getTerminalCode()).ifPresent((f) -> {
            throw new DuplicatedRecordFoundException("nickname");
        });
        UserEntity currentUser = loadUserEntityByUsername(request.getCurrentNickName(), request.getTerminalCode()).orElseThrow(() -> new NoMatchRecordFoundException("user"));
        currentUser.setLastEditDate(LocalDateTime.now());
        currentUser.setNickname(request.getNickName());
        userRepository.save(currentUser);
        xUserDetailService.removeXUserByUsernameAndChannelCode(currentUser, request.getTerminalCode());
        userCache.removeUserFromCache(userCacheKey(request.getCurrentNickName(), request.getTerminalCode()));
        return userMapper.toModel(currentUser);
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

    @Transactional
    public User updateUserLoginStaticPassword(PasswordModificationRequest request, boolean bypassCheckingOldPassword) {
        validatePasswordModificationRequest(request);
        UserEntity userEntity = findAuthenticatedUserByUsernameAndTerminalCode(request.getUsername(), request.getTerminalCode());
        if (!bypassCheckingOldPassword) {
            ValidationUtils.checkBlankString(request.getOldPassword(), () -> new InvalidInputException("oldPassword"));
            if (!passwordHashService.matchesLoginPassword(
                    request.getOldPassword(),
                    userEntity.getPerson().getUsername(),
                    userEntity.getLoginStaticPassword()
            )) {
                throw new InvalidInputException("oldPassword");
            }
        }
        userEntity.setLoginStaticPassword(passwordHashService.encodeNewPassword(request.getNewPassword()));
        userEntity.setLastDateOfFirstPasswordChange(LocalDate.now());
        userEntity.setLastEditDate(LocalDateTime.now());
        userRepository.save(userEntity);
        UserAuthentication currentAuthentication = AuthenticationUtils.getLoggedInUserAuthentication();
        assert currentAuthentication != null;
        String terminalCode = currentAuthentication.getTerminalCode();
        xUserDetailService.removeXUserByUsernameAndChannelCode(userEntity, terminalCode);
        userCache.removeUserFromCache(userCacheKey(request.getUsername(), request.getTerminalCode()));
        return userMapper.toModel(userEntity);
    }

    @Transactional
    public User updateUserTransactionStaticPass(PasswordModificationRequest request) {
        validatePasswordModificationRequest(request);
        UserEntity userEntity = findAuthenticatedUserByUsernameAndTerminalCode(request.getUsername(), request.getTerminalCode());
        if (!passwordHashService.matchesTransactionPassword(
                request.getOldPassword(),
                userEntity.getPerson().getUsername(),
                userEntity.getTransactionStaticPassword()
        )) {
            throw new InvalidInputException("oldPassword");
        }
        userEntity.setTransactionStaticPassword(passwordHashService.encodeNewPassword(request.getNewPassword()));
        userEntity.setLastEditDate(LocalDateTime.now());
        userRepository.save(userEntity);
        UserAuthentication currentAuthentication = AuthenticationUtils.getLoggedInUserAuthentication();
        assert currentAuthentication != null;
        String terminalCode = currentAuthentication.getTerminalCode();
        xUserDetailService.removeXUserByUsernameAndChannelCode(userEntity, terminalCode);
        userCache.removeUserFromCache(userCacheKey(request.getUsername(), request.getTerminalCode()));
        return userMapper.toModel(userEntity);
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
        if (newPassword.length() < 8 || StringUtils.isNumeric(newPassword)) {
            throw new PasswordSecurityConstraintsException("newPassword");
        }
    }

    private boolean hasAdministratorAccess() {
        UserAuthentication loggedInUserAuthentication = AuthenticationUtils.getLoggedInUserAuthentication();
        ValidationUtils.checkNull(loggedInUserAuthentication, AuthenticationRequiredException::new);
        assert loggedInUserAuthentication != null;
        return loggedInUserAuthentication.hasAuthority(SecurityConstants.ROLE_ADMIN_USER);
    }

    private void validateUserNickNameRequest(UserNickNameModifyRequest request) {
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

    @Transactional
    public Boolean activateOrDeactivateStatusUser(UpdateUserStatusRequest request) {
        ValidationUtils.checkNull(request.getStatus(), () -> new MissingRequiredInputException("status"));
        ValidationUtils.checkBlankString(request.getTerminalCode(), () -> new MissingRequiredInputException("terminalCode"));
        Optional<UserEntity> findUserEntity = findByNationalCodeAndTerminalIDAndPersonTypeAndSubOrganizationId(
                request.getPersonType(),
                request.getNationalId(),
                request.getSubOrganizationId(),
                request.getTerminalCode());
        UserEntity user = findUserEntity.orElseThrow(() -> new NoMatchRecordFoundException("user"));
        user.setStatus(Boolean.TRUE.equals(request.getStatus()) ? UserStatus.ACTIVE : UserStatus.INACTIVE);
        userRepository.save(user);
        xUserDetailService.removeXUserByUsernameAndChannelCode(user,request.getTerminalCode().toUpperCase());
        return UserStatus.getBooleanValue(user.getStatus());
    }

    public User createShahkarVerifiedUserAndDeleteOld(String nationalCode, String mobileNo, String terminalCode) {
        ValidationUtils.checkBlankString(nationalCode, () -> new MissingRequiredInputException("nationalCode"));
        ValidationUtils.checkInvalidMobileNumber(mobileNo, () -> new InvalidInputException("mobileNo"));
        ValidationUtils.checkBlankString(terminalCode, () -> new MissingRequiredInputException("terminalCode"));
        Optional<Terminal> terminal = terminalService.findTerminalByCode(terminalCode);
        ValidationUtils.checkEmptyOptional(terminal, () -> new InvalidInputException("terminalCode"));

        GeneralRealPersonEntity personEntity = findIndividualPersonByNationalCode(nationalCode);
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
            UserEntity userEntity  =   userEntities
                    .filter(list -> !list.isEmpty())
                    .map(list -> list.get(0))
                    .orElseThrow(() -> new IllegalArgumentException("User list is empty or not present"));
            return userMapper.toModel(userEntity);

        }

        return null;
    }

    public GeneralRealPersonEntity findRealPersonByNationalCode(String nationalCode) {
        return personRepository.findRealPersonByNationalCode(nationalCode);
    }

    public GeneralRealPersonEntity findIndividualPersonByNationalCode(String nationalCode) {
        return personRepository.findIndividualPersonByNationalCode(nationalCode);
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
//        entity.setOtpSerialNumber(request.getOtpSerialNumber());
//        entity.setPerson(personEntity);
//        entity.setCreatorBranch(request.getCreatorBranch());
//        entity.setCreator(creatorEntity.getId());
//        entity.setLastEditor(creatorEntity.getId());
        User user = userMapper.toModel(entity);

        GeneralPerson person = new UnknownPerson();
        person.setId(new Random().nextInt());
        person.setUsername(StringUtils.generateGuid());
        person.setStatus(PersonStatus.ACTIVE);
        person.setMobile1(mobileNo);
        user.setPerson(person);

        return user;
    }

    @Transactional
    public User createUser(UserDataRequest request) {
        ValidationUtils.checkBlankString(request.getNickname(), () -> new MissingRequiredInputException("nickname"));
        ValidationUtils.checkBlankString(request.getTerminalCode(), () -> new MissingRequiredInputException("terminalCode"));
        ValidationUtils.checkNull(request.getCreatorBranch(), () -> new MissingRequiredInputException("creatorBranch"));
        ValidationUtils.checkNull(request.getPersonId(), () -> new MissingRequiredInputException("personId"));
        ValidationUtils.checkNull(request.getLoginAuthenticationMethod(), () -> new InvalidInputException("loginAuthenticationMethod"));
        ValidationUtils.checkNull(request.getTransactionAuthenticationMethod(), () -> new InvalidInputException("transactionAuthenticationMethod"));
        Terminal terminal = terminalService.findTerminalByCode(request.getTerminalCode()).orElseThrow(() -> new InvalidInputException("terminalCode"));

        if (AuthenticationMethod.STATIC_PASSWORD.equals(request.getLoginAuthenticationMethod()) &&
            StringUtils.isEmpty(request.getLoginStaticPassword())) {
            throw new MissingRequiredInputException("loginStaticPassword");
        }
        if (AuthenticationMethod.STATIC_PASSWORD.equals(request.getTransactionAuthenticationMethod()) &&
            StringUtils.isEmpty(request.getTransactionStaticPassword())) {
            throw new MissingRequiredInputException("transactionStaticPassword");
        }
        if (!userRepository.findByPersonIdAndLegacyTerminalId(request.getPersonId(), terminal.getLegacyTerminalId().intValue()).isEmpty()) {
            throw new DuplicatedRecordFoundException("terminal");
        }
        GeneralPersonEntity personEntity = findPersonById(request.getPersonId());
        ValidationUtils.checkNull(personEntity, () -> new InvalidInputException("personId"));
        GeneralPersonEntity creatorEntity = findPersonByUsername(AuthenticationUtils.getLoggedInGlobalUsername());
        ValidationUtils.checkNull(creatorEntity, () -> new NoMatchRecordFoundException("creator person does not login"));
        assert creatorEntity != null;
        UserEntity entity = mapToUserEntity(request, terminal.getLegacyTerminalId().intValue(), creatorEntity, personEntity, request.getUserType());
        entity = userRepository.save(entity);
        return userMapper.toModel(entity);
    }

    private UserEntity mapToUserEntity(UserDataRequest request, Integer terminalId, GeneralPersonEntity creatorEntity, GeneralPersonEntity personEntity, UserType userType) {
        UserEntity entity = new UserEntity();
        entity.setNickname(request.getNickname());
        entity.setTerminalId(terminalId);
        entity.setLoginAuthenticationMethod(request.getLoginAuthenticationMethod());
        entity.setTransactionAuthenticationMethod(request.getTransactionAuthenticationMethod());
        entity.setAccessParameters(validateAccessParameter(request.getAccessParameters()));
        entity.setStatus(UserStatus.ACTIVE);
        entity.setLoginStaticPassword(encodeNewPasswordIfPresent(request.getLoginStaticPassword()));
        entity.setTransactionStaticPassword(encodeNewPasswordIfPresent(request.getTransactionStaticPassword()));
        entity.setOtpSerialNumber(request.getOtpSerialNumber());
        entity.setPerson(personEntity);
        entity.setCreatorBranch(request.getCreatorBranch());
        entity.setCreator(creatorEntity.getId());
        entity.setLastEditor(creatorEntity.getId());
        entity.setCreateDate(LocalDateTime.now());
        entity.setLastEditDate(LocalDateTime.now());
        entity.setPrintCount(0);
        entity.setType(Objects.nonNull(userType) ? userType : null);
        return entity;
    }

    private String encodeNewPasswordIfPresent(String rawPassword) {
        return StringUtils.isBlank(rawPassword) ? rawPassword : passwordHashService.encodeNewPassword(rawPassword);
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
                userMapper.toModels(entities.getContent()));
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

        User user = userMapper.toModel(userEntity.get());
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

    public Optional<List<String>> loadUserAuthorities(Integer personId) {
        List<RoleEntity> roles = roleRepository.findByPersonId(personId);
        if (null == roles || roles.isEmpty())
            return Optional.empty();
        return Optional.of(roles.stream()
                .map(RoleEntity::getCode)
                .collect(Collectors.toList()));
    }

//    public boolean checkUserActivationCode(String terminalCode, String username, String accessParameter, String activationCode) {
//        List<UserActivationEntity> activationEntities = userActivationRepository.findAllByTerminalCodeAndUsernameAndAccessParameterAndActivationCodeAndActivatedTrue(
//                terminalCode, username, accessParameter, activationCode);
//        return null != activationEntities && !activationEntities.isEmpty();
//    }

    @Transactional
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


    @Transactional
    public User updateLoginPasswordMethod(AuthenticationMethodModificationRequest request) {
        validateAuthenticationMethodModificationRequest(request);
        UserAuthentication loggedInUserAuthentication = AuthenticationUtils.getLoggedInUserAuthentication();
        ValidationUtils.checkNull(loggedInUserAuthentication, AuthenticationRequiredException::new);
        assert loggedInUserAuthentication != null;
        AuthenticationMethod requestMethod = request.getAuthenticationMethod();
        if (!requestMethod.equals(AuthenticationMethod.STATIC_PASSWORD)) {
            OtpType otpType = OtpType.toOtpType(requestMethod);
            verifyOtpCode(loggedInUserAuthentication, request.getCredential(), OtpReason.CHANGE_LOGIN_AUTHENTICATION_METHOD, otpType);
        }
        UserEntity userEntity = findUser(loggedInUserAuthentication);
        String terminalCode = loggedInUserAuthentication.getTerminalCode();
        String nickname = loggedInUserAuthentication.getName();
        userEntity.setLastEditDate(LocalDateTime.now());
        userEntity.setLoginAuthenticationMethod(requestMethod);
        userRepository.save(userEntity);
        xUserDetailService.removeXUserByUsernameAndChannelCode(userEntity, terminalCode);
        userCache.removeUserFromCache(userCacheKey(nickname, terminalCode));
        return userMapper.toModel(userEntity);
    }

    public void verifyOtpCode(UserAuthentication loggedInUserAuthentication, String credential, OtpReason reason, OtpType otpType) {
        OtpVerifyRequest otpVerifyRequest = OtpVerifyRequest.builder()
                .otpType(otpType)
                .recipient(getCurrentRecipient(loggedInUserAuthentication))
                .reason(reason)
                .user(loggedInUserAuthentication.getPrincipal())
                .claimCode(credential)
                .build();
        OtpVerifyResponse otpVerifyResponse = otpService.verifyOtp(otpVerifyRequest);
        if (!otpVerifyResponse.isSuccessful()) {
            String source = otpVerifyResponse.getOtpType() != null && OtpType.DEVICE.equals(otpVerifyResponse.getOtpType()) ? "otpDevice" : "otpCode";
            throw new InvalidInputException(source);
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
        validateTransactionMethodChangeServiceAccess(currentTxMethod, requestMethod, userEntity, headerClaimCode, request, currentUserAuthentication);
        userEntity.setLastEditDate(LocalDateTime.now());
        userEntity.setTransactionAuthenticationMethod(requestMethod);
        String terminalCode = currentUserAuthentication.getTerminalCode();
        String nickname = currentUserAuthentication.getName();
        userRepository.save(userEntity);
        xUserDetailService.removeXUserByUsernameAndChannelCode(userEntity, terminalCode);
        userCache.removeUserFromCache(userCacheKey(nickname, terminalCode));
        return userMapper.toModel(userEntity);
    }

    @Transactional
    public User changeAuthenticationMethodByNationalCodeAndTerminalId(ChangeAuthenticationMethodRequest request) {
        ValidationUtils.checkNull(request.getAuthenticationMethod(), () -> new InvalidInputException("authenticationMethod"));
        ValidationUtils.checkNull(request.getAuthenticationMethodType(), () -> new InvalidInputException("authenticationMethodType"));
        ValidationUtils.checkBlankString(request.getNationalCode(), () -> new InvalidInputException("nationalCode"));
        ValidationUtils.checkBlankString(request.getTerminalCode(), () -> new InvalidInputException("terminalCode"));
        UserEntity userEntity = findByNationalCodeAndTerminalIDAndSubOrganizationId(request.getNationalCode(), request.getSubOrganization(), request.getTerminalCode())
                .orElseThrow(() -> new NoMatchRecordFoundException("user"));
        switch (request.getAuthenticationMethodType()) {
            case TRANSACTION -> userEntity.setTransactionAuthenticationMethod(request.getAuthenticationMethod());
            case LOGIN -> userEntity.setLoginAuthenticationMethod(request.getAuthenticationMethod());
            default ->
                    throw new UnsupportedOperationException("Unsupported authentication method: " + request.getAuthenticationMethodType());
        }
        userRepository.save(userEntity);
        xUserDetailService.removeXUserByUsernameAndChannelCode(userEntity, request.getTerminalCode());
        userCache.removeUserFromCache(userCacheKey(userEntity.getNickname(), request.getTerminalCode()));
        return userMapper.toModel(userEntity);
    }

    private void validateTransactionMethodChangeServiceAccess(AuthenticationMethod currentTxMethod
            , AuthenticationMethod requestTxMethod
            , UserEntity userEntity
            , String claimCode
            , AuthenticationMethodModificationRequest request
            , UserAuthentication currentUserAuthentication) {
        if (currentTxMethod.equals(AuthenticationMethod.STATIC_PASSWORD)) {
            checkStaticPassword(userEntity, claimCode);
            OtpType otpType = OtpType.toOtpType(requestTxMethod);
            verifyOtpCode(currentUserAuthentication, request.getCredential(), OtpReason.CHANGE_TRANSACTION_AUTHENTICATION_METHOD, otpType);
        } else {
            OtpType targetOtpType = OtpType.toOtpType(currentTxMethod);
            verifyOtpCode(currentUserAuthentication, claimCode, OtpReason.CHANGE_TRANSACTION_AUTHENTICATION_METHOD, targetOtpType);
            if (AuthenticationMethod.STATIC_PASSWORD.equals(requestTxMethod)) {
                checkStaticPassword(userEntity, request.getCredential());
            } else {
                OtpType currentOtpTypeSource = OtpType.toOtpType(requestTxMethod);
                verifyOtpCode(currentUserAuthentication, request.getCredential(), OtpReason.CHANGE_TRANSACTION_AUTHENTICATION_METHOD, currentOtpTypeSource);
            }
        }
    }

    public void checkStaticPassword(UserEntity userEntity, String credential) {
        if (!passwordHashService.matchesTransactionPassword(
                credential,
                userEntity.getPerson().getUsername(),
                userEntity.getTransactionStaticPassword()
        )) {
            throw new InvalidInputException("static password");
        }
    }

    public boolean validateStaticPassword(User user, String credential, AuthenticationMethodType authenticationMethodType) {
        if (authenticationMethodType == null) {
            return false;
        }
        if (authenticationMethodType.equals(AuthenticationMethodType.TRANSACTION)) {
            return passwordHashService.matchesTransactionPassword(
                    credential,
                    user.getPerson().getUsername(),
                    user.getTransactionStaticPassword()
            );
        }
        return passwordHashService.matchesLoginPassword(
                credential,
                user.getPerson().getUsername(),
                user.getLoginStaticPassword()
        );
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
        xUserDetailService.removeXUserByUsernameAndChannelCode(userEntity, terminal.getCode());
        userCache.removeUserFromCache(userCacheKey(request.getNickname(), terminal.getCode()));
        return userMapper.toModel(userEntity);
    }

    private void applyDynamicUpdateChanges(UserEntity userEntity, UserDataChangeRequest request) {
        DynamicUpdateUtils.applyChangesIfNotBlankOrNull(request.getNickname(), userEntity::setNickname);
        DynamicUpdateUtils.applyChangesIfNotBlankOrNull(request.getOtpSerialNumber(), userEntity::setOtpSerialNumber);
        DynamicUpdateUtils.applyChangesIfNotBlankOrNull(
                request.getLoginStaticPassword(),
                rawPassword -> {userEntity.setLoginStaticPassword(passwordHashService.encodeNewPassword(rawPassword))
                userEntity.setLastDateOfFirstPasswordChange(LocalDate.now());
       });
        DynamicUpdateUtils.applyChangesIfNotBlankOrNull(
                request.getTransactionStaticPassword(),
                rawPassword -> userEntity.setTransactionStaticPassword(passwordHashService.encodeNewPassword(rawPassword))
        );
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
        return userMapper.toModel(userRepository.findById(userId).orElseThrow(() -> new NoMatchRecordFoundException("userId")));
    }

    public List<UserEntity> findByPersonIdAndLegacyTerminalCode(Integer userId, String terminalCode) {
        Terminal terminal = findTerminalByCode(terminalCode);
        return userRepository.findByPersonIdAndLegacyTerminalId(userId, terminal.getLegacyTerminalId().intValue());
    }

    public User findByNicknameAndTerminalCode(String nickname, String terminalCode) {
        Terminal terminal = terminalService.findTerminalByCode(terminalCode)
                .orElseThrow(() -> new InvalidInputException("terminalCode"));
        List<UserEntity> userEntities = findByNicknameAndLegacyTerminalId(nickname, terminal.getLegacyTerminalId().intValue());
        if (Objects.nonNull(userEntities) && !userEntities.isEmpty()) {
            return userMapper.toModel(userEntities.get(0));
        }
        return null;
    }

    public List<UserEntity> findByNicknameAndLegacyTerminalId(String nickname, Integer legacyTerminalId) {
        return userRepository.findByNicknameAndLegacyTerminalId(nickname, legacyTerminalId);
    }

    public List<UserEntity> findByNicknameAndLegacyTerminalCode(String nickname, String terminalCode) {
        Terminal terminal = findTerminalByCode(terminalCode);
        return userRepository.findByNicknameAndLegacyTerminalId(nickname, terminal.getLegacyTerminalId().intValue());
    }

    public Optional<UserEntity> findByNationalCodeAndTerminalIDAndSubOrganizationId(String nationalCode, String subOrganization, String terminalCode) {
        Terminal terminal = findTerminalByCode(terminalCode);
        ValidationUtils.checkBlankString(nationalCode, () -> new MissingRequiredInputException("nationalCode"));
        ValidationUtils.checkNull(terminalCode, () -> new NoMatchRecordFoundException("terminalCode"));
        Specification<UserEntity> spec = FindUserByNationalCodeSpecs.toSpecification(null, nationalCode, subOrganization, terminal.getLegacyTerminalId());
        return userRepository.findOne(spec);
    }

    public Optional<UserEntity> findByNationalCodeAndTerminalIDAndPersonTypeAndSubOrganizationId(PersonType personType, String nationalCode, String subOrganization, String terminalCode) {
        Terminal terminal = findTerminalByCode(terminalCode);
        ValidationUtils.checkNull(personType, () -> new MissingRequiredInputException("personType"));
        ValidationUtils.checkBlankString(nationalCode, () -> new MissingRequiredInputException("nationalCode"));
        ValidationUtils.checkNull(terminalCode, () -> new NoMatchRecordFoundException("terminalCode"));
        Specification<UserEntity> spec = FindUserByNationalCodeSpecs.toSpecification(personType, nationalCode, subOrganization, terminal.getLegacyTerminalId());
        return userRepository.findOne(spec);
    }

    public List<GeneralPersonEntity> findOtpRegistration(String nationalCode, List<String> tokenTypes) {
        return personRepository.findOtpRegistration(nationalCode, tokenTypes);
    } //TODO move to personService

    public List<GeneralPersonEntity> findByTokenTypesAndNationalCode(String nationalCode, List<String> tokenTypes) {
        return personRepository.findByTokenTypesAndNationalCode(nationalCode, tokenTypes);
    }//TODO move to personService

    public List<GeneralPersonEntity> findByTokenTypesAndNationalCodeAndOtpSerialNo(String nationalCode, List<String> tokenTypes, String otpSerialNo) {
        return personRepository.findByTokenTypesAndNationalCodeAndOtpSerialNo(nationalCode, tokenTypes, otpSerialNo);
    }//TODO move to personService

    @Transactional
    public User assignTerminalToPerson(UserAssignTerminalRequest request) {
        ValidationUtils.checkBlankString(request.getOtpCode(),()-> new InvalidInputException("otpCode"));
        verifyOtp(request);
        findByNationalCodeAndTerminalIDAndPersonTypeAndSubOrganizationId(request.getPersonType(), request.getNationalId(), request.getSubOrganizationId(), request.getTerminalCode()).ifPresent(userEntity -> {
            throw new DuplicatedRecordFoundException(request.getNationalId());
        });
        String roleCode;
        if (request.getTerminalCode().equalsIgnoreCase(TerminalType.IB.getTerminalCode())
            || request.getTerminalCode().equalsIgnoreCase(TerminalType.NIB.getTerminalCode())
            || request.getTerminalCode().equals(TerminalType.MB.getTerminalCode())) {
            roleCode = CUSTOMER_ROLE_CODE;
        } else {
            throw new InvalidInputException("TerminalCode");
        }
        GeneralPersonEntity generalPerson = findPerson(request.getPersonType(), request.getNationalId(), request.getSubOrganizationId())
                .orElseThrow(() -> new NoMatchRecordFoundException("person"));
        UserEntity userEntity = createUserEntity(request, generalPerson);
        userRepository.save(userEntity);
        userRepository.flush();
        uPersonService.addPersonRole(userEntity.getPerson().getId(), roleCode);
        return userMapper.toModel(userEntity);
    }

    @Transactional
    public User assignTerminalToPersonWithoutOtp(UserAssignTerminalRequest request) {
        findByNationalCodeAndTerminalIDAndPersonTypeAndSubOrganizationId(request.getPersonType(), request.getNationalId(), request.getSubOrganizationId(), request.getTerminalCode()).ifPresent(userEntity -> {
            throw new DuplicatedRecordFoundException(request.getNationalId());
        });
        String roleCode;
        if (request.getTerminalCode().equalsIgnoreCase(TerminalType.IB.getTerminalCode())
                || request.getTerminalCode().equalsIgnoreCase(TerminalType.NIB.getTerminalCode())
                || request.getTerminalCode().equals(TerminalType.MB.getTerminalCode())) {
            roleCode = CUSTOMER_ROLE_CODE;
        } else {
            throw new InvalidInputException("TerminalCode");
        }
        GeneralPersonEntity generalPerson = findPerson(request.getPersonType(), request.getNationalId(), request.getSubOrganizationId())
                .orElseThrow(() -> new NoMatchRecordFoundException("person"));
        UserEntity userEntity = createUserEntity(request, generalPerson);
        userRepository.save(userEntity);
        userRepository.flush();
        uPersonService.addPersonRole(userEntity.getPerson().getId(), roleCode);
        return userMapper.toModel(userEntity);
    }

    private void verifyOtp(UserAssignTerminalRequest request) {
        String terminalCode = extractRequestTerminalCode();
        String accessParameter = extractRequestAccessParameter().orElseThrow(() -> new MissingRequiredInputException("accessParameter"));
        GeneralPersonEntity generalPerson = findPerson(request.getPersonType(),request.getNationalId(),request.getSubOrganizationId())
                .orElseThrow(()->new NoMatchRecordFoundException("user"));
        Recipient recipient = Recipient.builder()
                .address(generalPerson.getMobile1())
                .identifier(generalPerson.getMobile1())
                .identifierType(UserIdentifierType.MOBILE_NUMBER)
                .terminalCode(terminalCode)
                .accessParameter(accessParameter)
                .build();
        OtpVerifyRequest otpRequest = OtpVerifyRequest.builder()
                .otpType(OtpType.SMS)
                .reason(OtpReason.BANK_CONSOLE_CUSTOMER_VERIFICATION)
                .claimCode(request.getOtpCode())
                .recipient(recipient)
                .build();
        OtpVerifyResponse otpVerifyResponse = otpService.verifyOtp(otpRequest);
        if (!otpVerifyResponse.isSuccessful()){
            throw new InvalidInputException("otpCode");
        }
    }

    private GeneralLegalPersonEntity findLegalPerson(String nationalId, String subOrganizationId) {
        return personRepository.findGeneralLegalPersonEntityByNationalIdAndSubOrganizationId(nationalId, subOrganizationId);
    }

    private GeneralPersonEntity findLegalPerson(String nationalId) {
        List<GeneralLegalPersonEntity> resultList = personRepository.findGeneralLegalPersonEntityByNationalId(nationalId);
        if (resultList.isEmpty()) {
            throw new InvalidInputException("person with nationalId '" + nationalId + "' subOrg must not be empty.");
        } else if (resultList.size() > 1) {
            throw new DuplicatedRecordFoundException("person with nationalId '" + nationalId + "' has more than one record.");
        } else {
            return resultList.get(0);
        }
    }

    private Optional<GeneralPersonEntity> findPerson(PersonType personType, String nationalId, String subOrganizationId) {
        switch (personType) {
            case REAL, EMPLOYEE -> {
                return Optional.ofNullable(findRealPersonByNationalCode(nationalId));
            }
            case CORPORATE, BANK, TAMIN, GOVERNANCE -> {
                if (Objects.nonNull(subOrganizationId) && !subOrganizationId.isBlank()) {
                    return Optional.ofNullable(findLegalPerson(nationalId, subOrganizationId));
                } else {
                    return Optional.ofNullable(findLegalPerson(nationalId));
                }
            }
            default -> throw new InvalidInputException("personType");
        }
    }

    private String generateUniqueNickName(String terminalCode) {
        String nickName = credentialGenerator.generateNumericUsername();
        while (Objects.nonNull(findByNicknameAndTerminalCode(nickName, terminalCode))) {
            nickName = credentialGenerator.generateNumericUsername();
        }
        return nickName;
    }

    private UserEntity createUserEntity(UserAssignTerminalRequest request, GeneralPersonEntity generalPerson) {
        ValidationUtils.checkBlankStringIfNotNull(request.getPhoneNumber(), () -> new MissingRequiredInputException("phoneNumber"));
        ValidationUtils.checkNull(request.getLoginAuthenticationMethod(), () -> new MissingRequiredInputException("loginAuthenticationMethod"));
        ValidationUtils.checkEmptyString(request.getNationalId(), () -> new MissingRequiredInputException("nationalId"));
        ValidationUtils.checkEmptyString(request.getTerminalCode(), () -> new MissingRequiredInputException("terminalCode"));
        ValidationUtils.checkNull(request.getTerminalCode(), () -> new MissingRequiredInputException("terminalCode"));
        ValidationUtils.checkNull(request.getPersonType(), () -> new MissingRequiredInputException("personType"));
        ValidationUtils.checkNull(request.getTransactionAuthenticationMethod(), () -> new MissingRequiredInputException("transactionAuthenticationMethod"));
        if (request.getLoginAuthenticationMethod().equals(AuthenticationMethod.STATIC_PASSWORD)) {
            ValidationUtils.checkBlankString(request.getLoginStaticPassword(), () -> new MissingRequiredInputException("loginStaticPassword"));
        }
        if (request.getTransactionAuthenticationMethod().equals(AuthenticationMethod.STATIC_PASSWORD)) {
            ValidationUtils.checkBlankString(request.getTransactionStaticPassword(), () -> new MissingRequiredInputException("transactionStaticPassword"));
        }
        Terminal terminal = findTerminalByCode(request.getTerminalCode());
        Integer loggedInUserId = AuthenticationUtils.getLoggedInUserId();
        String nickName = generateUserNickName(request, generalPerson);
        UserEntity user = new UserEntity();
        user.setNickname(nickName);
        user.setAccessParameters(Set.of(user.getPerson().getMobile1())); //TODO How fill it?
        user.setTerminalId(terminal.getLegacyTerminalId().intValue());
        user.setLoginAuthenticationMethod(request.getLoginAuthenticationMethod());
        user.setTransactionAuthenticationMethod(request.getLoginAuthenticationMethod());
        user.setStatus(UserStatus.ACTIVE);
        user.setPrintCount(0);
        user.setLoginStaticPassword(passwordHashService.encodeNewPassword(Optional.ofNullable(request.getLoginStaticPassword()).orElse("BLANK")));
        user.setTransactionStaticPassword(passwordHashService.encodeNewPassword(Optional.ofNullable(request.getTransactionStaticPassword()).orElse("BLANK")));
        user.setPerson(generalPerson);
        user.setType(UserType.CM_REGULAR); //TODO Is the type set correctly?
        user.setCreatorBranch(getLoggedInBranchCode());
        user.setCreator(loggedInUserId);
        user.setCreateDate(LocalDateTime.now());
        return user;
    }

    private String generateUserNickName(UserAssignTerminalRequest request, GeneralPersonEntity generalPerson) {
        String terminalCode = request.getTerminalCode();
        String nickName;
        if (terminalCode.equals(TerminalType.MB.getTerminalCode())) {
            nickName = StringUtils.isBlank(request.getNickName()) ? request.getPhoneNumber() : request.getNickName();
        } else if (generalPerson instanceof GeneralLegalPersonEntity legalPerson) {
            nickName = legalPerson.getNationalId();
        } else if (generalPerson instanceof GeneralRealPersonEntity realPerson) {
            nickName = realPerson.getNationalCode();
        } else {
            nickName = credentialGenerator.generateNumericUsername();
        }
        User foundUser = findByNicknameAndTerminalCode(nickName, terminalCode);
        if (Objects.nonNull(foundUser)) {
            nickName = generateUniqueNickName(terminalCode);
        }
        return nickName;
    }

    private String getLoggedInBranchCode() {
        User loggedInUser = AuthenticationUtils.getLoggedInUser();
        assert loggedInUser != null;
        GeneralPerson loggedInPerson = loggedInUser.getPerson();
        String branchCode = loggedInPerson.getBranchCode();
        if (StringUtils.isBlank(branchCode)) {
            GeneralPersonEntity generalPersonEntity = personRepository.findById(AuthenticationUtils.getLoggedInUserId())
                    .orElseThrow(() -> new NoMatchRecordFoundException("user"));
            branchCode = generalPersonEntity.getBranchCode();
        }
        //TODO BRANCH CODE MUST BE SET
//        ValidationUtils.checkBlankString(branchCode, () -> new MissingRequiredInputException("branchCode"));
        return Optional.ofNullable(branchCode).orElse("9999");
    }

    public Boolean  changePassword(UpdatePasswordRequest request) {
        ValidationUtils.checkEmptyString(request.getChannelCode(), () -> new MissingRequiredInputException("channelCode"));
        ValidationUtils.checkEmptyString(request.getNationalCode(), () -> new MissingRequiredInputException("nationalCOde"));
        ValidationUtils.checkNull(request.getAuthenticationMethodType(), () -> new MissingRequiredInputException("authenticationMethodType"));
        UserEntity userEntity = findByNationalCodeAndTerminalIDAndSubOrganizationId(request.getNationalCode(), request.getSubOrganizationId(), request.getChannelCode()).orElseThrow(() -> new NoMatchRecordFoundException("user"));
        GeneralPersonEntity person = userEntity.getPerson();
        String generatedPassword = StringUtils.generateRandomString(8);
        if (request.getAuthenticationMethodType().equals(LOGIN)) {
            if (userEntity.getLoginAuthenticationMethod().equals(AuthenticationMethod.OTP) || userEntity.getLoginAuthenticationMethod().equals(AuthenticationMethod.PUBLIC_KEY)) {
                throw new UnsupportedOperationException();
            }

            userEntity.setLoginStaticPassword(passwordHashService.encodeNewPassword(generatedPassword));
            userEntity.setLastDateOfFirstPasswordChange(LocalDate.now());
        } else {
            if (userEntity.getLoginAuthenticationMethod().equals(AuthenticationMethod.OTP) || userEntity.getLoginAuthenticationMethod().equals(AuthenticationMethod.PUBLIC_KEY)) {
                throw new UnsupportedOperationException();
            }
            userEntity.setTransactionStaticPassword(passwordHashService.encodeNewPassword(generatedPassword));
        }
        sendNotification(request,userEntity,generatedPassword);
        xUserDetailService.removeXUserByUsernameAndChannelCode(userEntity, request.getChannelCode());
            userCache.removeUserFromCache(userCacheKey(userEntity.getNickname(), request.getChannelCode()));
        userRepository.save(userEntity);
        return true;
    }

    private void sendNotification(UpdatePasswordRequest request, UserEntity userEntity, String generatedPassword) {
        try {
            NotificationTemplate template = request.getAuthenticationMethodType().equals(LOGIN) ? RESET_FIRST_PASSWORD : RESET_SECOND_PASSWORD;
            Terminal terminal = terminalService.findTerminalByCode(request.getChannelCode()).orElseThrow(()->new InvalidInputException("channelCode"));
            NotificationData notificationData = createNotificationData(userEntity.getNickname(), terminal.getTitle(),generatedPassword);
            GeneralPersonEntity person = userEntity.getPerson();
            //CREATE ISSUER INFO
            IssuerInfo issuerInfo = IssuerInfo.builder()
                    .personType(person.getPersonType())
                    .personUsername(person.getUsername())
                    .terminalCode(terminal.getCode())
                    .build();
            //CREATE RECIPIENT
            Recipient recipient = Recipient.builder()
                    .address(person.getMobile1())
                    .identifier(userEntity.getNickname())
                    .identifierType(UserIdentifierType.USER_NICKNAME)
                    .terminalCode(terminal.getCode())
                    .build();
            //CREATE NOTIFICATION REQUEST
            NotificationRequest notificationRequest = NotificationRequest.builder()
                    .template(template)
                    .media(NotificationMedia.SMS)
                    .recipient(recipient)
                    .userLocale(new Locale("fa", "IR")) //TODO GET FROM HEADER
                    .data(notificationData)
                    .terminalCode(request.getChannelCode())
                    .issuerInfo(issuerInfo)
                    .build();
            notificationService.sendNotification(notificationRequest);
        } catch (Exception e) {
            log.error("Exception occurred while sending notification ", e);
        }
    }

    private NotificationData createNotificationData(String nickName, String channelTitle,String generatedPassword) {
        return new NotificationData()
                .put(NotificationDataKey.CHANNEL_TITLE, channelTitle)
                .put(NotificationDataKey.USER_NICKNAME, nickName)
                .put(NotificationDataKey.PASSWORD, generatedPassword);
    }
}
