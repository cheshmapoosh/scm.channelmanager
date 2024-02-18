package ir.daneshrefah.scm.notification.repository;


import ir.daneshrefah.scm.notification.domain.NotificationLogEntity;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-05
 */
@Repository
public interface NotificationLogRepository extends CrudRepository<NotificationLogEntity, Long> {
}
