package ir.daneshrefah.scm.uaa.common.model.location;

import ir.daneshrefah.scm.uaa.common.model.BaseModel;

// BRANCH Table
/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public class Branch extends BaseModel {

    private String code;
    private String title;
    private BranchHead branchHead;

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

    public BranchHead getBranchHead() {
        return branchHead;
    }

    public void setBranchHead(BranchHead branchHead) {
        this.branchHead = branchHead;
    }

    @Override
    public String toString() {
        return "Branch{" +
                "code='" + code + '\'' +
                ", title='" + title + '\'' +
                ", branchHead=" + branchHead +
                ", id=" + getId() +
                '}';
    }
}
