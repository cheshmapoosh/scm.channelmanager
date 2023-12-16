package ir.daneshrefah.scm.uaa.common.model.location;

// BRANCH_HEAD Table

import ir.daneshrefah.scm.uaa.common.model.BaseModel;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public class BranchHead extends BaseModel {
    private String code;
    private String title;
    private String description;


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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "BranchHead{" +
                "id=" + getId() +
                ", code='" + code + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
