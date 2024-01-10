package ir.daneshrefah.scm.core.entity.condition;

import ir.daneshrefah.scm.common.data.entity.TerminalEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "TBL_SCM_TERMINAL_CONDITION")
@Getter
@Setter
public class TerminalConditionEntity extends ConditionBaseEntity<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "TERMINAL_CONDITION_ID")
    private String id;

    @ManyToOne
    @JoinColumn(name = "TERMINAL_ID")
    private TerminalEntity terminalEntity;

}
