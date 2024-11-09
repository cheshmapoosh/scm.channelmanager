package ir.daneshrefah.scm.common.error;

import ir.daneshrefah.scm.common.BaseModel;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-29
 */
@Getter
@Setter
public class ErrorMapping extends BaseModel<Long> {

    private String providerId;
    private String providerErrorCode;
    private String errorMessage;
    /**
     * if errorMessage in ${...msg...} pattern,
     * the errorMess filled by real message without ${}
     * and just set true value of bundleKey.
     */
    private boolean bundleKey;
    private Integer scmErrorCode;
    private String exceptionOverrideName;
    private MessageStatus status;

}
