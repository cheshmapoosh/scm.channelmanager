package ir.daneshrefah.scm.common.data.entity.terminal;

import ir.daneshrefah.scm.common.data.converter.TerminalStatusConverter;
import ir.daneshrefah.scm.common.data.entity.AbstractDefaultEntity;
import ir.daneshrefah.scm.common.model.terminal.TerminalStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "TBL_SCM_TERMINAL")
public class TerminalEntity extends AbstractDefaultEntity<String> {

    @Id
    @Column(name = "TERMINAL_ID")
    private String id;
    private String code;
    private String title;
    @Convert(converter = TerminalStatusConverter.class)
    private TerminalStatus status;
    private Boolean supportCheckAuthentication;
    private Boolean supportCheckSecondAuthentication;
    private Boolean supportCheckServiceAccess;
    private Boolean supportCheckAssetAccess;

}
