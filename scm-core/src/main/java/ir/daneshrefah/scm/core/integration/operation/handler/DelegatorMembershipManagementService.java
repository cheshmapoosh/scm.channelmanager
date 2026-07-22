package ir.daneshrefah.scm.core.integration.operation.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.common.constant.CustomerRelationType;
import ir.daneshrefah.scm.common.constant.OperationCode;
import ir.daneshrefah.scm.common.data.entity.asset.*;
import ir.daneshrefah.scm.common.data.entity.person.GeneralPersonEntity;
import ir.daneshrefah.scm.common.data.entity.person.GeneralRealPersonEntity;
import ir.daneshrefah.scm.common.data.entity.person.IndividualPersonEntity;
import ir.daneshrefah.scm.common.data.mapper.MembershipMapper;
import ir.daneshrefah.scm.common.data.mapper.PersonMapper;
import ir.daneshrefah.scm.common.data.repository.PersonRepository;
import ir.daneshrefah.scm.common.data.repository.assets.AccountRepository;
import ir.daneshrefah.scm.common.data.repository.assets.CustomerAccountRepository;
import ir.daneshrefah.scm.common.data.repository.assets.CustomerRepository;
import ir.daneshrefah.scm.common.data.repository.assets.MembershipRepository;
import ir.daneshrefah.scm.common.exception.AuthenticationRequiredException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.model.membership.MembershipType;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.common.model.person.PersonType;
import ir.daneshrefah.scm.core.services.auth.UaaApi;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
//import ir.daneshrefah.scm.uaa.repository.authentication.UserChannelAuthentication;
//import ir.daneshrefah.scm.uaa.repository.authentication.UserChannelAuthenticationRepository;
//import ir.daneshrefah.scm.uaa.service.user.XUserDetailService;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class DelegatorMembershipManagementService extends AbstractJavaService {
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final PersonRepository personRepository;
    private final MembershipRepository membershipRepository;
    private final CustomerAccountRepository customerAccountRepository;
    private final UaaApi uaaApi;

    public DelegatorMembershipManagementService(ServiceProducerTemplate producerTemplate,
                                                ObjectMapper objectMapper,
                                                CustomerRepository customerRepository,
                                                AccountRepository accountRepository,
                                                PersonRepository personRepository,
                                                MembershipRepository membershipRepository,
                                                CustomerAccountRepository customerAccountRepository,
                                                UaaApi uaaApi
    ) {
        super(producerTemplate, objectMapper);
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.personRepository = personRepository;
        this.membershipRepository = membershipRepository;
        this.customerAccountRepository = customerAccountRepository;
        this.uaaApi = uaaApi;
    }

    @JavaService(operationCode = OperationCode.SVC_GRANT_FUND_TRANSFER)
    @Transactional
    public Long delegatoMembershipManagement(Exchange exchange) {
        JsonNode body = (JsonNode) exchange.getProperty(Message.ORIGINAL_BODY);
        log.info("Delegating to membership management service started!");
        String accountNo = getText(body, "accountNo");
        String customerNo = getFirstCustomer(body);
        String nationalCode = getText(body, "nationalCode");
        Boolean isCreate = parseCreateFlag(body);

        validate(accountNo, customerNo, isCreate);

        GeneralPersonEntity person = getKarpardaz(nationalCode);
        try {
            log.info("current personId (karpardaz) is {}, nationalCode {}", person.getId(), ((IndividualPersonEntity)person).getNationalCode());
        }catch (ClassCastException e) {}

        if (!isCreate) {
            return deactivateDelegator(person, customerNo);
        }
        Long memId = createDelegator(person, customerNo, accountNo);
        removeXuserDetail(person.getId(), exchange);
        return memId;
    }

    private void removeXuserDetail(int userId, Exchange exchange) {
        log.info("start Removing user {} from membership manager", userId);
        String authorization = exchange.getIn().getHeader("Authorization", String.class);
        if (Objects.isNull(authorization) || !authorization.startsWith("Bearer ")) {
            throw new AuthenticationRequiredException();
        }
        String token = authorization.split(" ")[1];
        uaaApi.removeXUserByUsername(userId, token);
    }

    private Long createDelegator(GeneralPersonEntity person, String customerNo, String accountNo) {
        log.info("creating delegator membership for customerNo={}", customerNo);

        MembershipEntity membershipEntity = getMembership(person, customerNo);
        if (Objects.isNull(membershipEntity)) {
            membershipEntity = new MembershipEntity();
            membershipEntity.setArchiveNumber(0);
            membershipEntity.setDefaultAccount(false);
            membershipEntity.setPerson(person);
            membershipEntity.setCustomerNo(customerNo);
            membershipEntity.setMembershipType(MembershipType.DELEGATOR);
            membershipEntity.setCustomerAccount(getCustomerAccount(person, customerNo, accountNo));
        }
        membershipEntity.setActiveDelegate(true);

        return membershipRepository.save(membershipEntity).getId();
    }

    private MembershipEntity getMembership(GeneralPersonEntity person, String customerNo) {
        MembershipEntity membershipEntity = membershipRepository.findMembershipListByUserIdAndMembershipTypeAndCustomerNo(
                person.getId(),
                MembershipType.DELEGATOR,
                customerNo);
        return membershipEntity;
    }

    private Long deactivateDelegator(GeneralPersonEntity person, String customerNo) {
        MembershipEntity membershipEntity = getMembership(person, customerNo);

        if (Objects.isNull(membershipEntity)) {
            throw new NoMatchRecordFoundException("active delegator karpardaz membership not found! customerNo : " + customerNo);
        }

        membershipEntity.setActiveDelegate(false);
        MembershipEntity savedDelegatorMembership = membershipRepository.save(membershipEntity);
        log.info("deActive delegator karpardaz membership! customerNo {} & membershipId {}", customerNo, savedDelegatorMembership.getId());
        return savedDelegatorMembership.getId();
    }

    private CustomerAccountEntity getCustomerAccount(GeneralPersonEntity person, String customerNo, String accountNo) {
        CustomerAccountEntity customerAccount = null;
//        CustomerEntity customer = getLegalCustomer(customerNo);
        CustomerEntity customer = getKarpardazCustomer(person);
        AccountEntity account = getLegalAccount(accountNo);
        Optional<CustomerAccountEntity> opt = customerAccountRepository.findByCustomerAndAccount(customer, account);
        if (opt.isPresent()) {
            return opt.get();
        }
        customerAccount = new CustomerAccountEntity();
        customerAccount.setCustomer(customer);
        customerAccount.setAccount(account);
        customerAccount.setRelationType(CustomerRelationType.ATTORNEY);
        CustomerAccountEntity savesCustomerAccount = customerAccountRepository.save(customerAccount);
        log.info("customer account with customerNo {} and accountNo {} prepared", customerNo, accountNo);
        return savesCustomerAccount;
    }

    private AccountEntity getLegalAccount(String accountNo) {
        Optional<AccountEntity> account = accountRepository.findByAccountNo(accountNo);
        if (!account.isPresent()) {
            log.info("Account with account no {} does not exist", accountNo);
            throw new NoMatchRecordFoundException("legal account not found! accountNo : " + accountNo);
        }
        return account.get();
    }

    private CustomerEntity getLegalCustomer(String customerNo) {
        Optional<CustomerEntity> customer = customerRepository.findByCustomerNo(customerNo);
        if (!customer.isPresent()) {
            log.info("Customer with no {} does not exist", customerNo);
            throw new NoMatchRecordFoundException("legal customer not found! customerNo : " + customerNo);
        }
        return customer.get();
    }

    private CustomerEntity getKarpardazCustomer(GeneralPersonEntity person){
        MembershipEntity karpardazMember = membershipRepository.findMembershipListByUserId(person.getId()).get(0);
        if (Objects.isNull(karpardazMember)) {
            log.info("membership with user id {} does not exist", person.getId());
            throw new NoMatchRecordFoundException("karpardaz membership not found! user id : " + person.getId());
        }
        if(Objects.isNull(karpardazMember.getMembershipType())) {
            log.info("customer account with membership id {} does not exist", karpardazMember.getId());
            throw new NoMatchRecordFoundException("karpardaz customer account not found! membership id : " + karpardazMember.getId());
        }
        return karpardazMember.getCustomerAccount().getCustomer();
    }

    private GeneralPersonEntity getKarpardaz(String nationalCode) {
        GeneralPersonEntity person = personRepository.findRealPersonByNationalCodeAndPersonType(nationalCode, PersonType.REAL);
        if (Objects.isNull(person)) {
            log.info("user (karpardaz) with national code {} does not exist", nationalCode);
            throw new NoMatchRecordFoundException("legal customer not found! customerNo : " + nationalCode);
        }
        return person;
    }

    private String getText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null ? null : value.asText();
    }

    private String getFirstCustomer(JsonNode node) {
        JsonNode customers = node.get("customers");
        return (customers != null && customers.size() > 0)
                ? customers.get(0).asText()
                : null;
    }

    private Boolean parseCreateFlag(JsonNode node) {
        JsonNode isCreate = node.get("insDel");
        return isCreate == null ? null : "1".equals(isCreate.asText().trim());
    }

    private void validate(String accountNo, String customerNo, Boolean isCreate) {
        if (accountNo == null || customerNo == null || isCreate == null) {
            throw new IllegalArgumentException("Invalid request parameters");
        }
    }
}
