package ir.daneshrefah.scm.core.integration.operation.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.common.constant.OperationCode;
import ir.daneshrefah.scm.common.data.entity.asset.AccountEntity;
import ir.daneshrefah.scm.common.data.entity.asset.CustomerAccountEntity;
import ir.daneshrefah.scm.common.data.entity.asset.CustomerEntity;
import ir.daneshrefah.scm.common.data.entity.asset.MembershipEntity;
import ir.daneshrefah.scm.common.data.mapper.MembershipMapper;
import ir.daneshrefah.scm.common.data.mapper.PersonMapper;
import ir.daneshrefah.scm.common.data.repository.assets.AccountRepository;
import ir.daneshrefah.scm.common.data.repository.assets.CustomerAccountRepository;
import ir.daneshrefah.scm.common.data.repository.assets.CustomerRepository;
import ir.daneshrefah.scm.common.data.repository.assets.MembershipRepository;
import ir.daneshrefah.scm.common.exception.AuthenticationRequiredException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.model.membership.MembershipType;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.extern.slf4j.Slf4j;
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
                                                PersonMapper personMapper,
                                                MembershipMapper membershipMapper) {
        super(producerTemplate, objectMapper);
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.membershipRepository = membershipRepository;
        this.customerAccountRepository = customerAccountRepository;
        this.personMapper = personMapper;
    }

    @JavaService(operationCode = OperationCode.SVC_GRANT_FUND_TRANSFER)
    @Transactional
    public Long delegatoMembershipManagement(String customerNo, String accountNo, boolean isCreate) {
        log.info("Delegating to membership management service");
        Optional<AccountEntity> account = accountRepository.findByAccountNo(accountNo);
        if (!account.isPresent()) {
            log.info("Account with account no {} does not exist", accountNo);
            throw new RuntimeException("legal account not found! accountNo: " + accountNo);
        }

        Optional<CustomerEntity> customer = customerRepository.findByCustomerNo(customerNo);
        if (!customer.isPresent()) {
            log.info("Customer with no {} does not exist", customerNo);
            throw new RuntimeException("legal Customer not found! customerNo : " + customerNo);
        }
        CustomerAccountEntity customerAccount = new CustomerAccountEntity();
        customerAccount.setCustomer(customer.get());
        customerAccount.setAccount(account.get());
        Optional<CustomerAccountEntity> opt = customerAccountRepository.findByCustomerAndAccount(customer.get(), account.get());
        CustomerAccountEntity savesCustomerAccount;
        if (!opt.isPresent()) {
            savesCustomerAccount = customerAccountRepository.save(customerAccount);
            log.info("customer account with customerNo {} and accountNo {} created", customerNo, accountNo);
        } else {
            savesCustomerAccount = opt.get();
            log.info("customer account with customerNo {} and accountNo {} fetched", customerNo, accountNo);
        }

        GeneralPerson person = getCurrentPerson();
        log.info("current personId (karpardaz) is {}", person.getId());

        MembershipEntity membershipEntity;
        if (isCreate) {
            log.info("creating delegator karpardaz membership");
            membershipEntity = new MembershipEntity();
            membershipEntity.setArchiveNumber(0);
            membershipEntity.setDefaultAccount(false);
            membershipEntity.setPerson(personMapper.toPersonEntity(person)); // real user (karpardaz)
            membershipEntity.setCustomerNo(customerNo); // legal customer no
            membershipEntity.setMembershipType(MembershipType.DELEGATOR);
            membershipEntity.setCustomerAccount(savesCustomerAccount);
        } else {
            Optional<MembershipEntity> entity = membershipRepository.findMembershipListByUserIdAndTypeAndCustomerNo(person.getId(), MembershipType.DELEGATOR, customerNo);
            if (entity.isPresent()) {
                log.info("deActive delegator karpardaz membership");
                membershipEntity = entity.get();
            } else {
                throw new RuntimeException("delegator membership not found!");
            }
        }

        membershipEntity.setActiveDelegate(isCreate);
        return membershipRepository.save(membershipEntity).getId();
    }

    private GeneralPerson getCurrentPerson() {
        var loggedInUser = AuthenticationUtils.getLoggedInUser();
        ValidationUtils.checkNull(loggedInUser, AuthenticationRequiredException::new);
        var person = Objects.requireNonNull(loggedInUser).getPerson();
        ValidationUtils.checkNull(person, () -> new NoMatchRecordFoundException("nationalId"));
        return person;
    }
}
