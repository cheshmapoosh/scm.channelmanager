package ir.daneshrefah.scm.notification.client.repository;


import ir.daneshrefah.scm.notification.client.repository.domain.NotificationQueueEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationQueueRepository extends CrudRepository<NotificationQueueEntity,String> {


}
