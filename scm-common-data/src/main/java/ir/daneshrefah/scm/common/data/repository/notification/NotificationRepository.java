package ir.daneshrefah.scm.common.data.repository.notification;


import ir.daneshrefah.scm.common.data.entity.notification.NotificationEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, String> {

    @Query(value = "select o from NotificationEntity o where o.status = 'QUEUE' or o.status = 'RE_TRYING' " )
    List<NotificationEntity> findAllAvailable(PageRequest request);

    @Query(value = "select o from NotificationEntity o where o.status = 'SENDING' and o.lastEditDate < :maxLocalDateTime" )
    List<NotificationEntity> findAllSending(PageRequest request, @Param("maxLocalDateTime") LocalDateTime maxLocalDateTime);

    @Query(value = "select count(o) from NotificationEntity o where o.status = 'QUEUE' or o.status = 'RE_TRYING' " )
    Long findAllAvailableCount();
}
