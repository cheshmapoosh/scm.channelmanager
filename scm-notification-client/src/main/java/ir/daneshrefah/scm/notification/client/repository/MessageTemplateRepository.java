package ir.daneshrefah.scm.notification.client.repository;


import ir.daneshrefah.scm.common.model.notification.constants.NotificationTemplate;
import ir.daneshrefah.scm.notification.client.repository.entity.MessageTemplateEntity;
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
    Optional<MessageTemplateEntity> findByCode(NotificationTemplate code);
}
