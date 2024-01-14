package ir.daneshrefah.scm.plugin.nab.repository;

import ir.daneshrefah.scm.common.model.person.Account;
import ir.daneshrefah.scm.common.model.person.AccountAsset;
import ir.daneshrefah.scm.common.model.person.AccountType;
import ir.daneshrefah.scm.common.model.person.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-14
 */
@RequiredArgsConstructor
@Repository
public class CustomerRepository {

    private final JdbcTemplate jdbcTemplate;

    public Long findPersonIdByPersonProfileId(String personProfileId) {
        String sql = "SELECT USER_ID FROM REF.USER WHERE USERNAME = ? ";
        List<Long> ids = jdbcTemplate.queryForList(sql, Long.class, personProfileId);
        if (null == ids || ids.size() < 1) {
            return null;
        }
        return ids.get(0);
    }

    public Customer findAccountListByPersonId(String personProfileId) {
        Long personId = findPersonIdByPersonProfileId(personProfileId);
        if (null == personId) {
            return null;
        }

        String sql = "SELECT C.CUSTOMER_NO, C.PROVIDER_ID, AT.NAME as TYPE_NAME, AT.CODE as TYPE_CODE, A.ACCOUNT_NO, A.CLOSE, CA.ACCOUNT_ID, " +
                "M.NICK_NAME, M.DEFAULT_ACCOUNT " +
                "FROM REF.MEMBERSHIP M " +
                "INNER JOIN REF.CUSTOMERACCOUNT CA ON M.CUSTOMER_ACCOUNT_ID = CA.CUSTOMER_ACCOUNT_ID " +
                "INNER JOIN REF.CUSTOMER C ON CA.CUSTOMER_ID = C.CUSTOMER_ID " +
                "INNER JOIN REF.ACCOUNT A ON CA.ACCOUNT_ID = A.ACCOUNT_ID " +
                "INNER JOIN REF.ACCOUNT_TYPE AT ON A.ACCOUNT_TYPE_ID = AT.ACCOUNT_TYPE_ID " +
                "WHERE USER_ID = ?";

        AtomicReference<String> customerNo = new AtomicReference<>();
        AtomicReference<String> providerId = new AtomicReference<>();

        List<AccountAsset> assets = jdbcTemplate.query(sql,
                ps -> ps.setLong(1, personId),
                (rs, rowNum) -> {
                    customerNo.set(rs.getString("CUSTOMER_NO"));
                    providerId.set(rs.getString("PROVIDER_ID"));
                    String accountTypeCode = rs.getString("TYPE_CODE");
                    String accountTypeName = rs.getString("TYPE_NAME");
                    String accountNo = rs.getString("ACCOUNT_NO");
                    Boolean close = rs.getBoolean("CLOSE");
//                    Long accountId = rs.getLong("ACCOUNT_ID");
                    String nickname = rs.getString("NICK_NAME");
//                    Boolean defaultAccount = rs.getBoolean("DEFAULT_ACCOUNT");

                    AccountType accountType = new AccountType(accountTypeCode, accountTypeName);
                    Account account = Account.builder()
                            .accountType(accountType)
                            .accountNo(null != accountNo ? accountNo.trim() : null)
                            .nickname(null != nickname ? nickname.trim() : null)
                            .close(null != close ? close : false)
                            .build();

                    AccountAsset asset = AccountAsset.builder()
                            .account(account)
                            .build();

                    return asset;
                });

        Customer customer = new Customer();
        customer.setCustomerNo(customerNo.get());
        customer.setProviderId(providerId.get());
        customer.setAssets(assets);

        return customer;
    }

}
