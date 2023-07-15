package ir.daneshrefah.scm.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;
import java.time.LocalTime;

@MappedSuperclass
public abstract class AbstractEntity implements Serializable {

    @Column(name = "CREATOR")
    private String creator;
    @Column(name = "LAST_EDITOR")
    private String lastEditor;
    @Column(name = "CREATE_DATE")
    private LocalTime createDate;
    @Column(name = "LAST_EDIT_DATE")
    private LocalTime lastEditDate;

    public abstract String getId();

    public abstract void setId(String id);

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getLastEditor() {
        return lastEditor;
    }

    public void setLastEditor(String lastEditor) {
        this.lastEditor = lastEditor;
    }

    public LocalTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalTime createDate) {
        this.createDate = createDate;
    }

    public LocalTime getLastEditDate() {
        return lastEditDate;
    }

    public void setLastEditDate(LocalTime lastEditDate) {
        this.lastEditDate = lastEditDate;
    }
}
