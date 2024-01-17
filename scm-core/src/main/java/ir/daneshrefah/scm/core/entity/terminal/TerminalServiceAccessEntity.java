package ir.daneshrefah.scm.core.entity.terminal;

import ir.daneshrefah.scm.common.data.entity.AbstractDefaultEntity;
import ir.daneshrefah.scm.common.data.entity.TerminalEntity;
import ir.daneshrefah.scm.core.entity.service.ServiceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-22
 */
@Getter
@Setter
@Entity
@Table(name = "TBL_SCM_TERMINAL_SERVICE_ACCESS")
public class TerminalServiceAccessEntity extends AbstractDefaultEntity<String> {

    @Id
    @Column(name = "TERMINAL_SERVICE_ACCESS_ID")
    private String id;
    @ManyToOne
    @JoinColumn(name = "service_id")
    private ServiceEntity service;
    @ManyToOne
    @JoinColumn(name = "terminal_id")
    private TerminalEntity terminal;

}
