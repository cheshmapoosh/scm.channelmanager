package ir.daneshrefah.scm.common.model.notification;

import ir.daneshrefah.scm.common.BaseModel;
import ir.daneshrefah.scm.common.model.notification.constants.TemplateCode;
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
public class MessageTemplate extends BaseModel<Long> {

    private Long id;
    private TemplateCode code;
    private String title;
    private String body;
    private Integer tryCount;
    private Integer maxMinutesExpiration;
    private boolean isSystemic;

}
