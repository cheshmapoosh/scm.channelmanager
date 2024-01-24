package ir.daneshrefah.scm.core.entity.person;

import ir.daneshrefah.scm.common.data.entity.AbstractDefaultEntity;
import ir.daneshrefah.scm.common.data.entity.terminal.TerminalEntity;
import ir.daneshrefah.scm.core.entity.service.ServiceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-13
 */
@Getter
@Setter
@Entity
@Table(name = "TBL_SCM_PERSON_SERVICE_ACCESS")
public class ServiceAccessEntity extends AbstractDefaultEntity<Long> {

    @Id
    @Column(name = "PERSON_SERVICE_ACCESS_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String personProfileId;
    @ManyToOne
    @JoinColumn(name = "SERVICE_ID")
    private ServiceEntity service;
    @ManyToOne
    @JoinColumn(name = "TERMINAL_ID")
    private TerminalEntity terminal;
    private String assetId;

}
