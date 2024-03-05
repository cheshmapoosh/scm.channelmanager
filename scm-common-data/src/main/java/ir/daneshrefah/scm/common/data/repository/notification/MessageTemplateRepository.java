package ir.daneshrefah.scm.common.data.repository.notification;


import ir.daneshrefah.scm.common.model.notification.constants.TemplateCode;
import ir.daneshrefah.scm.common.data.entity.notification.MessageTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-05
 */
@Repository
public interface MessageTemplateRepository extends JpaRepository<MessageTemplateEntity, Long> {
    Optional<MessageTemplateEntity> findByCode(TemplateCode code);
}
