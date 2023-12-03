package ir.daneshrefah.scm.config.model.entity;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "TBL_SFG_APPLICATION")
public class ApplicationEntity implements Serializable {

    @Id
    @Column(name = "APPLICATION_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column(name = "CODE")
    private String code;

    @Column(name = "TITLE")
    private String title;



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
}

