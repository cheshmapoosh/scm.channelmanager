package ir.daneshrefah.scm.plugin.pichack.provider.dto.common;

import ir.daneshrefah.scm.plugin.api.model.message.AbstractRequestDTO;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-06
 */
@Getter
@Setter
public abstract class BasePichackRequest extends AbstractRequestDTO {

    private String sayadId;

}
