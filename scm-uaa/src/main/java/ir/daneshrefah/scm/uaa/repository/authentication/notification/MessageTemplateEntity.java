package ir.daneshrefah.scm.uaa.repository.authentication.notification;

import ir.daneshrefah.scm.common.data.entity.AbstractDefaultEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-05
 */
@Getter
@Setter
@Entity
@Table(name = "TBL_SNT_NOTIFICATION_LOG")
public class MessageTemplateEntity extends AbstractDefaultEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MESSAGE_TEMPLATE_ID")
    private Long id;
    private String code;

}
