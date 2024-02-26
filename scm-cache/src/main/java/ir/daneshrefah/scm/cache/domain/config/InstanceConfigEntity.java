package ir.daneshrefah.scm.cache.domain.config;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
public class InstanceConfigEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "INSTANCE_ID")
    private String id;

    @Column(name = "INSTANCE_NAME")
    private String instanceName;

    @Column(name = "STATISTICS_ENABLED")
    private Boolean statisticsEnabled;

}
