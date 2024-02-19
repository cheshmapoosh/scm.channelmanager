package ir.daneshrefah.scm.plugin.api.model.error;

import ir.daneshrefah.scm.common.BaseModel;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.common.model.service.ExternalServiceProvider;
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
public class ErrorMapping extends BaseModel<String> {

    private ExternalServiceProvider provider;
    private String providerErrorCode;
    private Integer scmErrorCode;
    private MessageStatus status;
    private String message;

}
