package ir.daneshrefah.scm.common.data.converter;

import ir.daneshrefah.scm.common.data.model.CmAccountType;
import ir.daneshrefah.scm.common.data.model.NabAccountType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

public class AccountTypeConverter {
    //    private java.util.Map<AccountStatus, AccountState> map = new HashMap<ir.dpi.cm.core.nab.AccountStatus, AccountState>();
    private static AccountTypeConverter instance;
    private Log log = LogFactory.getLog(AccountTypeConverter.class);

    public static AccountTypeConverter getInstance() {
        if (instance == null) {
            instance = new AccountTypeConverter();
        }
        return instance;
    }

    private java.util.Map<NabAccountType, CmAccountType> map = new HashMap<NabAccountType, CmAccountType>();

    public AccountTypeConverter() {
        map.put(NabAccountType.CURRENT_ACCOUNT, CmAccountType.CCA);
        map.put(NabAccountType.MONEY_LOANED_ACCOUNT, CmAccountType.ILA);
        map.put(NabAccountType.SAVING_ACCOUNT, CmAccountType.SDA);
//        map.put(NabAccountType.SPECIAL_SHORT_TERM_DEPOSIT_ACCOUNT, CmAccountType.CLA);
        map.put(NabAccountType.LONG_TERM_DEPOSIT_ACCOUNT, CmAccountType.LOC);
        map.put(NabAccountType.GOM_BONDS, CmAccountType.GOM);
        map.put(NabAccountType.UNKNOWN_ACCOUNT, CmAccountType.UNKNOWN);
    }

    public CmAccountType convert(String code) {
        NabAccountType accountType = findAccountTypeByCode(code);
        return (accountType != null) ? map.get(accountType) : CmAccountType.UNKNOWN;
    }

    private NabAccountType findAccountTypeByCode(String code) {
        NabAccountType[] accountTypes = NabAccountType.values();
        for (NabAccountType accountType : accountTypes) {
            if (accountType.getCode().equals(code)) {
                return accountType;
            }
        }
        log.warn("AccountType code: " + code + " did not match any map in " + NabAccountType.class);
        return null;
    }

    public NabAccountType convertCmAccountTypeToNab(CmAccountType accType) {
        Map<CmAccountType, NabAccountType> map = new HashMap<CmAccountType, NabAccountType>();
        map.put(CmAccountType.SDA, NabAccountType.SAVING_ACCOUNT);
        map.put(CmAccountType.CCA, NabAccountType.CURRENT_ACCOUNT);
        map.put(CmAccountType.ILA, NabAccountType.MONEY_LOANED_ACCOUNT);

        map.put(CmAccountType.CLA, NabAccountType.UNKNOWN_ACCOUNT);

        map.put(CmAccountType.LOC, NabAccountType.LONG_TERM_DEPOSIT_ACCOUNT);
        map.put(CmAccountType.UNKNOWN, NabAccountType.UNKNOWN_ACCOUNT);
        return map.get(accType);
    }

    public static CmAccountType convertNabAccountTypeToCm(NabAccountType accType) {
        Map<NabAccountType, CmAccountType> map = new HashMap<NabAccountType, CmAccountType>();
        map.put(NabAccountType.SAVING_ACCOUNT, CmAccountType.SDA);
        map.put(NabAccountType.CURRENT_ACCOUNT, CmAccountType.CCA);
        map.put(NabAccountType.MONEY_LOANED_ACCOUNT, CmAccountType.ILA);
        map.put(NabAccountType.UNKNOWN_ACCOUNT, CmAccountType.CLA);
        map.put(NabAccountType.LONG_TERM_DEPOSIT_ACCOUNT, CmAccountType.LOC);
        map.put(NabAccountType.UNKNOWN_ACCOUNT, CmAccountType.UNKNOWN);
        return map.get(accType);
    }
}
