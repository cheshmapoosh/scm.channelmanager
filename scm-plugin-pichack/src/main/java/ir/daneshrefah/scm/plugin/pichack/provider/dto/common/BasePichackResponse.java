package ir.daneshrefah.scm.plugin.pichack.provider.dto.common;

import ir.daneshrefah.scm.plugin.api.model.message.AbstractResponseDTO;
import lombok.Data;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-06
 */
@Data
public abstract class BasePichackResponse<T> extends AbstractResponseDTO {

    private Integer httpStatusCode;
    private String errorCode;
    private List<String> errorMessage;
    private String errorMessagePersian;

}
