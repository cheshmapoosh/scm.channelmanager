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
    EMPTY_CUSTOMER_NO("mt_cus"),
    INVALID_PHONE_NUMBER("invld_phn"),
    REJECTED("rjct"),
    EXPIRED_CARD("exp_crd"),
    SUSPECTED_FRAUD("sspctd_frd"),
    PRIVATE_CARD("pv_crd"),
    ALLOWABLE_NUMBER_ON_PIN_ENTRY_TRIES_EXCEEDED("pin_all"),
    REFER_TO_CARD_ISSUER("rf_crd_iss"),
    REFER_TO_CARD_ISSUER_SPECIAL_CONDITION("rf_crd_iss_spc"),
    BAD_MERCHANT("bd_mrchnt"),
    INVALID_AMOUNT("invld_amnt"),
    INVALID_CARD_NUMBER("invld_crd_no"),
    PIN_ELEMENT_REQUIRED_FOR_THIS_TRANSACTION_TYPE("pin_reqire"),
    UNKNOWN_CARD("unkhnown_crd"),
    FUNCTION_NOT_AVAILABLE("func_not_avil"),
    NO_SUFFICIENT_FUNDS("no_suf_fund"),
    INCORRECT_PIN("incrct_pin"),
    NO_SUCH_CARD("no_sch_crd"),
    CARD_HOLDER_TRANSACTION_NOT_PERMITTED("crd_hldr_not_prmt"),
    TERMINAL_TRANSACTION_NOT_PERMITTED("trmnl_not_prmt"),
    EXCEEDS_WITHDRAWAL_FREQUENCY_LIMIT("wthdrw_frqnc_lmt"),
    SECURITY_VIOLATION("scr_valid"),
    WITHDRAWAL_FREQUENCY_EXCEEDED("wthdrw_frqnc_excd"),
    CARD_NOT_IN_SERVICE("crd_not_in_srvc"),
    WRONG_PIN_FORMAT("wrng_pin_frmt"),
    ERROR_PIN_LENGTH("err_pin_ln"),
    CRYPTOGRAPHIC_ERROR("crpt_err"),
    DUPLICATE_BILL_PAYMENT_EXIST("dup_bl_py"),
    INVALID_BILLER_ID("invld_bller"),
    NO_CREDIT_ACCOUNT_2("no_crd_acc2"),
    NO_CREDIT_ACCOUNT("no_crd_acc"),
    NO_CHEQUE_ACCOUNT("no_chq_acc"),
    NO_SAVING_ACCOUNT("no_sv_acc"),
    BAD_CVV("bd_cvv"),
    INVALID_DATE("invld_date"),
    FUNCTION_NOT_AVAILABLE_TO_USER("func_not_avl_usr"),
    REJECTED_PICK_UP_CARD("rjct_pkup_crd"),
    EXPIRED_CARD_PICK_UP_CARD("exp_crd_pkup"),
    FRAUD_SUSPECTED_PICK_UP_CARD("frd_sspct_pkup_crd"),
    RESERVED_USAGE_PICK_UP_CARD("rvrs_usg_crd"),
    ISSUER_CALL_FOR_ACQUIRER_SECURITY_SERVICE_PICK_UP_CARD("iss_scr_crd"),
    NUMBER_OF_PIN_VALIDATION_ATTEMPTS_EXCEEDED("num_pin_vld_at"),
    SPECIAL_CONDITION_PICK_UP_CARD("spc_con_pkup_crd"),
    CARD_LOST_PICK_UP_CARD("crd_lst_pkup_crd"),
    CARD_STOLEN_PICK_UP_CARD("crd_stl_pkup_crd"),
    FRAUD_SUSPECTED_PICK_UP("frd_sspct_pkup_crd"),
    ALTERNATIVE_AMOUNT_REVERSED("alt_amnt_rvrs"),
    CARD_PICK_UP("crd_pkup"),
    SUCCESSFULLY_PROCESSED("sccs_prcs"),
    NOT_SUPPORTED_BY_RECEIVER("not_sprt_rcv"),
    UNABLE_TO_FIND_RECORD_IN_THE_FILE("unabl_fnd_rcrd"),
    DUPLICATE_RECORD_OLD_RECORD_REPLACED("dup_rcrd_old_rplc"),
    ZONE_CONTROL_ERROR("zn_ctrl_err"),
    FILE_LOCKED("file_lck"),
    UNSUCCESSFUL("unscces"),
    FORMAT_ERROR("frmt_err"),
    FILE_UNKNOWN("file_unknwn"),
    BALANCE_RECORD_CLEARED("blnc_rc_clr"),
    BALANCE_REQUEST("blnc_rq"),
    REVERSAL_ACCEPTED("rvrs_accpt"),
    INVALID_REVERSAL_AMOUNT("invld_rvrs_am"),
    COUNTERS_NOT_AVAILABLE("cntr_not_vld"),
    RECONCILIATION_DONE("rcn_don"),
    RECONCILIATION_PROCESS_NOT_AVAILABLE("rcn_proc_nt_avl"),
    CUT_OFF_IN_PROGRESS("ct_of_prg"),
    CONNECTION_NOT_ACCEPTED("conn_not_avl"),
    INVALID_TRANSACTION("invld_trn"),
    CARD_ISSUER_OR_SWITCH_INOPERATIVE("crd_iss_inop"),
    TRANSACTION_RECEIVER_NOT_REFERENCED_FOR_SWITCH("trns_svc_rf"),
    SYSTEM_DEFECT("dyd_dfct"),
    TIMEOUT("tm_out"),
    CARD_ISSUER_NOT_AVAILABLE("crd_iss_not_avl"),
    DUPLICATE_RECORD_NEW_RECORD_REJECTED("dup_rec_new_rjct"),
    RECORD_NOT_FOUND("rec_not_fnd"),
    ISSUER_NOT_FOUND("iss_not_fnd"),
    PIN_VERIFICATION_ERROR("pin_err"),
    TRANSACTION_PROCESSING_ERROR("trns_proc_err"),
    SERVER_PROCESSING_ERROR("srvr_err"),
    CONTACT_NOT_FOUND("CONTACT_NOT_FOUND"),
    MANA_VALIDATION_ERROR("mana_err");



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
