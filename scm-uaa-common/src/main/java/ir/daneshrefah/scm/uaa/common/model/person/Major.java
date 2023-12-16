package ir.daneshrefah.scm.uaa.common.model.person;

import ir.daneshrefah.scm.uaa.common.model.BaseModel;

// MAJOR Table
/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public class Major extends BaseModel {

    private String code;
    private String title;
    private Education education;

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

    public Education getEducation() {
        return education;
    }

    public void setEducation(Education education) {
        this.education = education;
    }

    @Override
    public String toString() {
        return "Major{" +
                "code='" + code + '\'' +
                ", title='" + title + '\'' +
                ", education=" + education +
                ", id=" + getId() +
                '}';
    }
}
