package ir.daneshrefah.scm.uaa.common.model.location;

import ir.daneshrefah.scm.uaa.common.model.BaseModel;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public class Address extends BaseModel {

    private String postalCode; // USER.POSTAL_CODE
    private String mobile; // USER.MOBILE
    private String phone; // USER.PHONE
    private String faxNumber; // USER.FAX_NUMBER

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFaxNumber() {
        return faxNumber;
    }

    public void setFaxNumber(String faxNumber) {
        this.faxNumber = faxNumber;
    }


    @Override
    public String toString() {
        return "Address{" +
                "postalCode='" + postalCode + '\'' +
                ", mobile='" + mobile + '\'' +
                ", phone='" + phone + '\'' +
                ", faxNumber='" + faxNumber + '\'' +
                '}';
    }
}
