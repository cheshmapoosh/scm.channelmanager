package ir.daneshrefah.scm.uaa.common.model.person;

import ir.daneshrefah.scm.uaa.common.model.location.Address;
import ir.daneshrefah.scm.uaa.common.model.location.Branch;
import ir.daneshrefah.scm.uaa.common.model.location.City;
import ir.daneshrefah.scm.uaa.common.type.Gender;
import ir.daneshrefah.scm.uaa.common.type.MaritalStatus;
import ir.daneshrefah.scm.uaa.common.type.Nationality;

import java.time.LocalDate;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public abstract class GeneralRealPerson extends GeneralPerson {

    private Gender gender; // USER.GENDER_ID
    private MaritalStatus maritalStatus; // USER.MARITAL_STATUS
    //    private String identityDocument; // USER.IDENTITY_DOCUMENT_TYPE refer to IDENTITY_DOCUMENT_TYPE Table
//    private String jobCode; // USER.JOB_CODE refer to CUSTOMER_JOB Table
    private Education education; //USER.EDUCATION_CODE refer to EDUCATION Table
    private Major major; //USER.MAJOR_CODE refer to MAJOR Table
    private Branch branch; // USER.BRANCH_CODE refer to BRANCH Table
    private Address address;
    private Nationality nationality; // USER.NATIONALITY_CODE
    private String nationalCode; //USER.NATIONAL_CODE
    private String birthPlace; // USER.BIRTH_PLACE
    private City issuePlace; // USER.ISSUE_PLACE
    private LocalDate birthDate; // USER.BIRTH_DATE
    private LocalDate issueDate; // USER.ISSUE_DATE
    private String identificationNo; // USER.IDENTIFICATION_NO
    private String identificationSerial; // USER.IDENTIFICATION_SERIAL
    private String identificationSerialNo; // USER.IDENTIFICATION_SERIAL_NO
    private String firstName; // USER.FIRST_NAME
    private String firstNameEnglish; // USER.FIRST_NAME_ENGLISH
    private String lastName; // USER.LAST_NAME
    private String lastNameEnglish;  // USER.LAST_NAME_ENGLISH
    private String fatherName; // USER.FATHER_NAME

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(MaritalStatus maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public Education getEducation() {
        return education;
    }

    public void setEducation(Education education) {
        this.education = education;
    }

    public Major getMajor() {
        return major;
    }

    public void setMajor(Major major) {
        this.major = major;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Nationality getNationality() {
        return nationality;
    }

    public void setNationality(Nationality nationality) {
        this.nationality = nationality;
    }

    public String getNationalCode() {
        return nationalCode;
    }

    public void setNationalCode(String nationalCode) {
        this.nationalCode = nationalCode;
    }

    public String getBirthPlace() {
        return birthPlace;
    }

    public void setBirthPlace(String birthPlace) {
        this.birthPlace = birthPlace;
    }

    public City getIssuePlace() {
        return issuePlace;
    }

    public void setIssuePlace(City issuePlace) {
        this.issuePlace = issuePlace;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public String getIdentificationNo() {
        return identificationNo;
    }

    public void setIdentificationNo(String identificationNo) {
        this.identificationNo = identificationNo;
    }

    public String getIdentificationSerial() {
        return identificationSerial;
    }

    public void setIdentificationSerial(String identificationSerial) {
        this.identificationSerial = identificationSerial;
    }

    public String getIdentificationSerialNo() {
        return identificationSerialNo;
    }

    public void setIdentificationSerialNo(String identificationSerialNo) {
        this.identificationSerialNo = identificationSerialNo;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFirstNameEnglish() {
        return firstNameEnglish;
    }

    public void setFirstNameEnglish(String firstNameEnglish) {
        this.firstNameEnglish = firstNameEnglish;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLastNameEnglish() {
        return lastNameEnglish;
    }

    public void setLastNameEnglish(String lastNameEnglish) {
        this.lastNameEnglish = lastNameEnglish;
    }

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    @Override
    public String toString() {
        return "GeneralRealPerson{" +
                "gender=" + gender +
                ", maritalStatus=" + maritalStatus +
                ", education=" + education +
                ", major=" + major +
                ", branch=" + branch +
                ", address=" + address +
                ", nationality=" + nationality +
                ", nationalCode='" + nationalCode + '\'' +
                ", birthPlace=" + birthPlace +
                ", issuePlace=" + issuePlace +
                ", birthDate=" + birthDate +
                ", issueDate=" + issueDate +
                ", identificationNo='" + identificationNo + '\'' +
                ", identificationSerial='" + identificationSerial + '\'' +
                ", identificationSerialNo='" + identificationSerialNo + '\'' +
                ", firstName='" + firstName + '\'' +
                ", firstNameEnglish='" + firstNameEnglish + '\'' +
                ", lastName='" + lastName + '\'' +
                ", lastNameEnglish='" + lastNameEnglish + '\'' +
                ", fatherName='" + fatherName + '\'' +
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
