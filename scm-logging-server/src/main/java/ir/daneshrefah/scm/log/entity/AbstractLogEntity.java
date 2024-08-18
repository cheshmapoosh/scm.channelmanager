package ir.daneshrefah.scm.log.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@MappedSuperclass
public abstract class AbstractLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "PAYLOAD")
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private String payload;
}
