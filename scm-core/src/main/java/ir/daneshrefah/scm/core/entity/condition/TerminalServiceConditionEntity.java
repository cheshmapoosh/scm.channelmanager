package ir.daneshrefah.scm.core.entity.condition;

import ir.daneshrefah.scm.core.entity.terminal.TerminalServiceAccessEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "TBL_SCM_TERMINAL_SERVICE_CONDITION")
@Getter
@Setter
public class TerminalServiceConditionEntity extends ConditionBaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "TERMINAL_SERVICE_CONDITION_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "TERMINAL_SERVICE_ACCESS_ID")
    private TerminalServiceAccessEntity terminalServiceAccessEntity;

}
