package ir.daneshrefah.scm.core.entity.condition;

import ir.daneshrefah.scm.core.entity.service.ServiceEntity;
import ir.daneshrefah.scm.core.entity.terminal.TerminalEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "TBL_SCM_TERMINAL_SERVICE_CONDITION")
@Getter
@Setter
public class TerminalServiceCondition extends ConditionBaseEntity<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID")
    private String id;

    @ManyToOne
    @JoinColumn(name = "TERMINAL_ID")
    private TerminalEntity terminalEntity;

    @ManyToOne
    @JoinColumn(name = "SERVICE_ID")
    private ServiceEntity serviceEntity;

}
