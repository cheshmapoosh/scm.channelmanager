package ir.daneshrefah.scm.core.entity.terminal;

import ir.daneshrefah.scm.common.data.entity.AbstractDefaultEntity;
import ir.daneshrefah.scm.common.data.entity.TerminalEntity;
import ir.daneshrefah.scm.core.entity.service.ServiceEntity;
import jakarta.persistence.*;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-22
 */
@Entity
@Table(name = "TBL_SCM_TERMINAL_SERVICE_ACCESS")
public class TerminalServiceAccessEntity extends AbstractDefaultEntity<String> {

    @Id
    @Column(name = "TERMINAL_SERVICE_ACCESS_ID")
    private String id;
    @ManyToOne
    @JoinColumn(name = "service_id")
    private ServiceEntity serviceEntity;
    @ManyToOne
    @JoinColumn(name = "terminal_id")
    private TerminalEntity terminalEntity;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public ServiceEntity getServiceEntity() {
        return serviceEntity;
    }

    public void setServiceEntity(ServiceEntity serviceEntity) {
        this.serviceEntity = serviceEntity;
    }

    public TerminalEntity getTerminalEntity() {
        return terminalEntity;
    }

    public void setTerminalEntity(TerminalEntity terminalEntity) {
        this.terminalEntity = terminalEntity;
    }
}
