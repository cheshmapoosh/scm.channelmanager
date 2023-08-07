package ir.daneshrefah.scm.plugin.api.model;

import java.io.Serializable;
import java.time.LocalTime;

public class BaseModel<T> implements Serializable {

    private T id;
    private String creator;
    private String lastEditor;
    private LocalTime createDate;
    private LocalTime lastEditDate;

    public T getId() {
        return id;
    }

    public void setId(T id) {
        this.id = id;
    }

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
