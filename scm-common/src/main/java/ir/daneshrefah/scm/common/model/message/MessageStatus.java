package ir.daneshrefah.scm.common.model.message;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-23
 */
@Getter
@RequiredArgsConstructor
public enum MessageStatus {

    SC_PROCESSING("sc_prg"), SC_SUCCESS("sc_scs"), SC_UNAUTHORIZED("sc_uat"), SC_ACCESS_DENIED("sc_acd"),
    SC_NOT_FOUND("sc_nfd"), SC_ERROR_VALIDATION("sc_evl"), SC_ERROR_SYSTEM("sc_esy"), SC_ERROR_BUSINESS("sc_ebz"),
    SC_ERROR_UNREACHABLE_PROVIDER("sc_eup"), SC_ERROR_DATA_INTEGRITY_VIOLATION("sc_div"),
    INVALID_SOURCE_ACCOUNT("nab_acc"),
    KARPARDAZ_NOT_EXSIST("nab_krprdz_del"),
    EMPTY_PRIVILEGES("mt_tt"),
    EMPTY_PERMIT_SERVICE_ID("mt_ps"),
    ERROR_VERIFYING_SIGNATORIES("err_emz"),
    CREATE_KARPARDAZ_FAILED("krprdz_err_ins"),
    DELETE_KARPARDAZ_DELETE("krprdz_err_del"),
    INVALID_EXPIRE_DATE("iv_tk"),
    INVALID_NATIONAL_CODE("iv_nc"),
    INVALID_INSDEL("iv_insdl"),
    INVALID_CARD_NO("iv_crd"),
    FAILED_CREATE_KARPARDAZ("krprdz_err_crt"),
    FAILED_CREATE_CUSTACC("err_ins_custacc"),
    FALED_DELETE_KARPARDAZ("krprdz_err_dl"),
    FAILED_DELETE_CUSTACC("err_dl_custacc"),
    ETC("sc_syr"),
    CUSTMER_ACCOUNT_CONFLICT("cnf_cus_acc"),
    EMPTY_CUSTOMER_NO("mt_cus");



    private final String code;

    public static MessageStatus findByCode(String code) {
        for (MessageStatus enumValue : MessageStatus.values()) {
            if (enumValue.getCode().equals(code)) {
                return enumValue;
            }
        }
        return null;
    }
}
