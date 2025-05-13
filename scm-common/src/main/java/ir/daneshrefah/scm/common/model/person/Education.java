package ir.daneshrefah.scm.common.model.person;


// EDUCATION Table

import ir.daneshrefah.scm.common.AbstractAuditableModel;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public class Education extends AbstractAuditableModel {

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
        return "Education{" +
                "code='" + code + '\'' +
                ", title='" + title + '\'' +
                ", id=" + getId() +
                '}';
    }
}
