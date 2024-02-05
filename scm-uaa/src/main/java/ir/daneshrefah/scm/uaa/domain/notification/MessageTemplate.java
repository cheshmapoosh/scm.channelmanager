package ir.daneshrefah.scm.uaa.domain.notification;

import ir.daneshrefah.scm.common.BaseModel;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-30
 */
@Data
public class MessageTemplate extends BaseModel {

    private String code;
    private String title;
    private String body;
    private boolean isSystemic;

}
