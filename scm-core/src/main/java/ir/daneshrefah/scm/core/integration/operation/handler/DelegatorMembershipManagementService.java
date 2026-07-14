package ir.daneshrefah.scm.core.integration.operation.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.common.constant.CustomerRelationType;
import ir.daneshrefah.scm.common.constant.OperationCode;
import ir.daneshrefah.scm.common.data.entity.asset.*;
import ir.daneshrefah.scm.common.data.mapper.MembershipMapper;
import ir.daneshrefah.scm.common.data.mapper.PersonMapper;
import ir.daneshrefah.scm.common.data.repository.assets.AccountRepository;
import ir.daneshrefah.scm.common.data.repository.assets.CustomerAccountRepository;
import ir.daneshrefah.scm.common.data.repository.assets.CustomerRepository;
import ir.daneshrefah.scm.common.data.repository.assets.MembershipRepository;
import ir.daneshrefah.scm.common.exception.AuthenticationRequiredException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.model.membership.MembershipType;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
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
    private final PersonMapper personMapper;
    private final MembershipRepository membershipRepository;
    private final CustomerAccountRepository customerAccountRepository;

    public DelegatorMembershipManagementService(ServiceProducerTemplate producerTemplate,
                                                ObjectMapper objectMapper,
                                                CustomerRepository customerRepository,
                                                AccountRepository accountRepository,
                                                MembershipRepository membershipRepository,
                                                CustomerAccountRepository customerAccountRepository,
                                                PersonMapper personMapper) {
        super(producerTemplate, objectMapper);
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.membershipRepository = membershipRepository;
        this.customerAccountRepository = customerAccountRepository;
        this.personMapper = personMapper;
    }

    @JavaService(operationCode = OperationCode.SVC_GRANT_FUND_TRANSFER)
    @Transactional
    public Long delegatoMembershipManagement(Exchange exchange) {
        JsonNode body = (JsonNode) exchange.getProperty(Message.ORIGINAL_BODY);
        log.info("Delegating to membership management service started!");
        String accountNo = getText(body, "accountNo");
        String customerNo = getFirstCustomer(body);
        Boolean isCreate = parseCreateFlag(body);

        validate(accountNo, customerNo, isCreate);

        GeneralPerson person = getCurrentPerson();
        log.info("current personId (karpardaz) is {}", person.getId());

        if (!isCreate) {
            return deactivateDelegator(person, customerNo);
        }
        Long memId = createDelegator(person, customerNo, accountNo);
        return memId;
    }

    private Long createDelegator(GeneralPerson person, String customerNo, String accountNo) {
        log.info("creating delegator membership for customerNo={}", customerNo);

        MembershipEntity membershipEntity = getMembership(person, customerNo);
        if (Objects.isNull(membershipEntity)) {
            membershipEntity = new MembershipEntity();
            membershipEntity.setArchiveNumber(0);
            membershipEntity.setDefaultAccount(false);
            membershipEntity.setPerson(personMapper.toPersonEntity(person));
//            membershipEntity.setCustomerNo(customerNo);
//            membershipEntity.setMembershipType(MembershipType.DELEGATOR);
            membershipEntity.setCustomerAccount(getCustomerAccount(customerNo, accountNo));
        }
//        membershipEntity.setActiveDelegate(true);

        return membershipRepository.save(membershipEntity).getId();
    }

    private MembershipEntity getMembership(GeneralPerson person, String customerNo) {
        return membershipRepository.findMembershipListByUserId(person.getId()).stream()
                .filter(membership -> Objects.equals("000", customerNo))
                .findFirst()
                .orElse(null);
    }



    private Long deactivateDelegator(GeneralPerson person, String customerNo) {
        MembershipEntity membershipEntity = getMembership(person, customerNo);

        if (Objects.isNull(membershipEntity)) {
            throw new NoMatchRecordFoundException("active delegator karpardaz membership not found! customerNo : " + customerNo);
        }

//        membershipEntity.setActiveDelegate(false);
        MembershipEntity savedDelegatorMembership = membershipRepository.save(membershipEntity);
        log.info("deActive delegator karpardaz membership! customerNo {} & membershipId {}", customerNo, savedDelegatorMembership.getId());
        return savedDelegatorMembership.getId();
    }

    private CustomerAccountEntity getCustomerAccount(String customerNo, String accountNo) {
        CustomerAccountEntity customerAccount = null;
        CustomerEntity customer = getLegalCustomer(customerNo);
        AccountEntity account = getLegalAccount(accountNo);
        Optional<CustomerAccountEntity> opt = customerAccountRepository.findByCustomerAndAccount(customer, account);
        if(opt.isPresent()) {
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

    private GeneralPerson getCurrentPerson() {
        var loggedInUser = AuthenticationUtils.getLoggedInUser();
        ValidationUtils.checkNull(loggedInUser, AuthenticationRequiredException::new);
        var person = Objects.requireNonNull(loggedInUser).getPerson();
        ValidationUtils.checkNull(person, () -> new NoMatchRecordFoundException("nationalId"));
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
