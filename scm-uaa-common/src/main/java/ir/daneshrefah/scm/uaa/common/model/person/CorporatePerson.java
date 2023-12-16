package ir.daneshrefah.scm.uaa.common.model.person;

import ir.daneshrefah.scm.uaa.common.model.location.Address;
import ir.daneshrefah.scm.uaa.common.model.location.City;
import ir.daneshrefah.scm.uaa.common.type.Nationality;
import ir.daneshrefah.scm.uaa.common.type.PersonType;

import java.time.LocalDate;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public class CorporatePerson extends GeneralPerson {

    private Nationality nationality; // USER.NATIONALITY_CODE
    private String nationalId; //USER.NATIONAL_CODE
    private String subOrganizationId; //USER.SUB_ORGANIZATION_ID
    private Address address;
    private City registerPlace; // USER.BIRTH_PLACE
    private LocalDate registerDate; // USER.BIRTH_DATE

    @Override
    public PersonType getType() {
        return PersonType.CORPORATE_CUSTOMER;
    }

    public Nationality getNationality() {
        return nationality;
    }

    public void setNationality(Nationality nationality) {
        this.nationality = nationality;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public String getSubOrganizationId() {
        return subOrganizationId;
    }

    public void setSubOrganizationId(String subOrganizationId) {
        this.subOrganizationId = subOrganizationId;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public City getRegisterPlace() {
        return registerPlace;
    }

    public void setRegisterPlace(City registerPlace) {
        this.registerPlace = registerPlace;
    }

    public LocalDate getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(LocalDate registerDate) {
        this.registerDate = registerDate;
    }


    @Override
    public String toString() {
        return "CorporatePerson{" +
                "nationality=" + nationality +
                ", nationalId='" + nationalId + '\'' +
                ", subOrganizationId='" + subOrganizationId + '\'' +
                ", address=" + address +
                ", registerPlace=" + registerPlace +
                ", registerDate=" + registerDate +
                ", type=" + getType() +
                ", active=" + getActive() +
                ", loyalty=" + getLoyalty() +
                ", region=" + getRegion() +
                ", city=" + getCity() +
                ", username='" + getUsername() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", id=" + getId() +
                '}';
    }
}
