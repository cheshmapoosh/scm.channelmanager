package ir.daneshrefah.scm.notification.repository;

import ir.daneshrefah.scm.notification.domain.NotificationQueueEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationQueueRepository extends CrudRepository<NotificationQueueEntity,String> {


}
