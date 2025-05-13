package ir.daneshrefah.scm.common.model.constant;

import ir.daneshrefah.scm.common.AbstractAuditableModel;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-06
 */
@Data
public class Constant extends AbstractAuditableModel<Long> {

    private String key;
    private String value;

}
