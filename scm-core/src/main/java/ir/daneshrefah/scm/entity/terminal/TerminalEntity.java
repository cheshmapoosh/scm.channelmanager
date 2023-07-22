package ir.daneshrefah.scm.entity.terminal;

import ir.daneshrefah.scm.common.model.terminal.Protocol;
import ir.daneshrefah.scm.entity.AbstractEntity;
import ir.daneshrefah.scm.repository.converter.ProtocolConverter;
import jakarta.persistence.*;

@Entity
@Table(name = "TBL_SCM_TERMINAL")
public class TerminalEntity extends AbstractEntity {
    @Id
    @Column(name = "TERMINAL_ID")
    private String id;
    private String code;
    private String title;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
