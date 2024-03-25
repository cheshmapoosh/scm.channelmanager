package ir.daneshrefah.scm.common.model.terminal;

import ir.daneshrefah.scm.common.BaseModel;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
@Getter
@Setter
public class Terminal extends BaseModel<String> {

    private String code;
    private String title;
    private Long legacyTerminalId;
    private TerminalStatus status;
    private boolean supportCheckAuthentication;
    private boolean supportCheckSecondAuthentication;
    private boolean supportCheckServiceAccess;
    private boolean supportCheckAssetAccess;

}
