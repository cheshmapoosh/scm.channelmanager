package ir.daneshrefah.scm.common.data.entity.distribution;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Entity
@Table(name = "TBL_SYN_DISTRIBUTED_LOCK")
@Getter
@Setter
@Accessors(chain = true)
public class DistributedLockEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "LOCK_GROUP")
    private String lockGroup;

    @Column(name = "LOCK_UNIQUE_PERSISTENCE_ID", unique = true)
    private String lockUniquePersistenceId;

    @Column(name = "INITIALIZED_TIME")
    private LocalDateTime lockInitializedTime;

    /* prevent from deadlock */
    @Column(name = "EXPIRATION_TIME")
    private LocalDateTime lockExpiration;

}
