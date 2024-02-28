package ir.daneshrefah.scm.common.data.repository.notification;


import ir.daneshrefah.scm.common.data.entity.notification.NotificationQueueEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationQueueRepository extends JpaRepository<NotificationQueueEntity, String> {

    @Query(value = "select o from NotificationQueueEntity o where o.status = 'QUEUE' or o.status = 'RE_TRYING' " )
    List<NotificationQueueEntity> findAllAvailable(PageRequest request);

    @Query(value = "select count(o) from NotificationQueueEntity o where o.status = 'QUEUE' or o.status = 'RE_TRYING' " )
    Long findAllAvailableCount();
}
