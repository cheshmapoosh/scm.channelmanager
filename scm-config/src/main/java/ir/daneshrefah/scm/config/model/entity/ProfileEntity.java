package ir.daneshrefah.scm.config.model.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "TBL_SFG_PROFILE")
public class ProfileEntity implements Serializable {

    @Id
    @Column(name = "PROFILE_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column(name = "CODE")
    private String code;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "CREATE_DATE")
    private LocalDateTime createDate;

    @Column(name = "LAST_EDIT_DATE")
    private LocalDateTime lastEditDate;

    @Column(name = "CREATOR_USER")
    private String creatorUser;

    @Column(name = "LAST_EDITOR_USER")
    private String lastEditorUser;


    public String getId() {
        return id;
    }

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

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public LocalDateTime getLastEditDate() {
        return lastEditDate;
    }

    public void setLastEditDate(LocalDateTime lastEditDate) {
        this.lastEditDate = lastEditDate;
    }

    public String getCreatorUser() {
        return creatorUser;
    }

    public void setCreatorUser(String creatorUser) {
        this.creatorUser = creatorUser;
    }

    public String getLastEditorUser() {
        return lastEditorUser;
    }

    public void setLastEditorUser(String lastEditorUser) {
        this.lastEditorUser = lastEditorUser;
    }
}
