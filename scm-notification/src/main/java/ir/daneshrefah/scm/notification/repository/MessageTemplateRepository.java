package ir.daneshrefah.scm.notification.repository;


import ir.daneshrefah.scm.notification.domain.MessageTemplateEntity;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
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
public interface MessageTemplateRepository extends CrudRepository<MessageTemplateEntity, Long> {
    Optional<MessageTemplateEntity> findByCode(String code);
}
