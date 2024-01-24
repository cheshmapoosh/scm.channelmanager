package ir.daneshrefah.scm.common.data.model.person;

import ir.daneshrefah.scm.common.BaseModel;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public class Loyalty extends BaseModel {

    private String code;
    private String title;

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

    @Override
    public String toString() {
        return "Loyalty{" +
                "code='" + code + '\'' +
                ", title='" + title + '\'' +
                ", id=" + getId() +
                '}';
    }
}
