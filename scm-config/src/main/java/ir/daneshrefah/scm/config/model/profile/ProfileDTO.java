package ir.daneshrefah.scm.config.model.profile;

import java.io.Serializable;

public class ProfileDTO implements Serializable {

    private String id;
    private String code;
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
