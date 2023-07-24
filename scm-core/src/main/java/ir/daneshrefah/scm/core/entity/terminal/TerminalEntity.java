package ir.daneshrefah.scm.core.entity.terminal;

import ir.daneshrefah.scm.core.entity.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "TBL_SCM_TERMINAL")
public class TerminalEntity extends AbstractEntity<String> {
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
