package ir.daneshrefah.scm.notification.domain;

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
@Table(name = "TBL_SNT_MESSAGE_TEMPLATE")
public class MessageTemplateEntity extends AbstractDefaultEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MESSAGE_TEMPLATE_ID")
    private Long id;
    @Column(name = "CODE")
    private String code;
    @Column(name = "TITLE")
    private String title;
    @Column(name = "BODY")
    private String body;
    @Column(name = "IS_SYSTEMIC")
    private boolean isSystemic;

}
