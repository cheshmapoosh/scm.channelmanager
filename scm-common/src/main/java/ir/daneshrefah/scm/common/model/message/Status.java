package ir.daneshrefah.scm.common.model.message;


/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-23
 */
public enum Status {

    SC_PROCESSING("sc_prg"), SC_SUCCESS("sc_scs"), SC_UNAUTHORIZED("sc_uat"), SC_ACCESS_DENIED("sc_acd"),
    SC_NOT_FOUND("sc_nfd"), SC_ERROR_VALIDATION("sc_evl"), SC_ERROR_SYSTEM("sc_esy"), SC_ERROR_BUSINESS("sc_ebz"),
    SC_ERROR_UNREACHABLE_PROVIDER("sc_eup"), SC_ERROR_DATA_INTEGRITY_VIOLATION("sc_div");

    Status(String code) {
        this.code = code;
    }

    private String code;

    public String getCode() {
        return code;
    }

    public static Status findByCode(String code) {
        for (Status enumValue : Status.values()) {
            if (enumValue.getCode().equals(code)) {
                return enumValue;
            }
        }
        return null;
    }
}
