package ir.daneshrefah.scm.uaa.common.model;

import java.io.Serializable;
import java.time.LocalTime;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public class BaseModel implements Serializable {

    private Object id;
    private String creator;
    private String lastEditor;
    private LocalTime createDate;
    private LocalTime lastEditDate;

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
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
