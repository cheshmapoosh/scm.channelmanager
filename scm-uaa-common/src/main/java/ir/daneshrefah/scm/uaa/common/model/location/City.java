package ir.daneshrefah.scm.uaa.common.model.location;

// CITY Table

import ir.daneshrefah.scm.uaa.common.model.BaseModel;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public class City extends BaseModel {

    private String code;
    private String title;
    private Region region;

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

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    @Override
    public String toString() {
        return "City{" +
                "code='" + code + '\'' +
                ", title='" + title + '\'' +
                ", region=" + region +
                ", id=" + getId() +
                '}';
    }
}
