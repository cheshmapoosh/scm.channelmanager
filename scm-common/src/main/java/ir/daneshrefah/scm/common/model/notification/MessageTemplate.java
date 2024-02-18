package ir.daneshrefah.scm.common.model.notification;

import ir.daneshrefah.scm.common.BaseModel;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-30
 */
@Setter
@Getter
public class MessageTemplate extends BaseModel<String> {

    private String id;
    private String code;
    private String title;
    private String body;
    private boolean isSystemic;

}
