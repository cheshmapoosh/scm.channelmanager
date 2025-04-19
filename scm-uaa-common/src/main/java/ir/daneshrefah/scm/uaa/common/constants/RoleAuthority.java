package ir.daneshrefah.scm.uaa.common.constants;
/**
 * @see ir.daneshrefah.scm.uaa.common.core.GrantAuthority
 */
public interface RoleAuthority {

    /* All session scope must be started with 'ROLE_' */

    String CUSTOMER             =  "ROLE_CUSTOMER";
    String CORPORATE_CUSTOMER   =  "ROLE_CORPORATE_CUSTOMER";
}
