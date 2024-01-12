package ir.daneshrefah.scm.uaa.repository.authentication;

import ir.daneshrefah.scm.common.data.entity.AbstractEntity;
import ir.daneshrefah.scm.uaa.common.type.Nationality;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-09
 */
@Getter
@Setter
@Entity
@Table(name = "USER")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "USER_TYPE", discriminatorType = DiscriminatorType.INTEGER)
public abstract class GeneralPersonEntity extends AbstractEntity<Integer> {

    @Id
    @Column(name = "USER_ID")
    private Integer id;
    private String username;
    private Boolean active;
    private String phone;
    private String mobile;
    private String email;
    @Column(name = "FAX_NUMBER")
    private String fax;
    private String address;
    private String postalCode;
    @Column(name = "NATIONALITY_CODE")
    @Convert(converter = NationalityConverter.class)
    private Nationality nationality;

//TODO    private String CUSTOMER_TYPE_CODE => REF.CUSTOMER_TYPE_CODE.CODE
//TODO    private String IDENTITY_DOCUMENT_TYPE
//TODO    private String JOB_CODE => REF.CUSTOMER_JOB.CODE
//TODO    private String REGION_CODE => REF.REGION.CODE
//TODO    private String EDUCATION_CODE => REF.EDUCATION.CODE = > diplom, lisans, ...
//TODO    private String MAJOR_CODE => REF.MAJOR
//TODO    private String CITY_CODE => REF.CITY
//TODO    private String BRANCH_CODE => REF.BRANCH
//TODO    private String IDENTIFICATION_NO => String
//TODO    private String ISSUE_PLACE
//TODO    private String IDENTIFICATION_SERIAL => String
//TODO    private String IDENTIFICATION_SERIAL_NO => String
//TODO    private String BIRTH_PLACE => String
//TODO    private String ISSUE_DATE

//UnUsed    private String TITLE_CODE => Mr/Ms
//UnUsed    private String CARD_TYPE_CODE

}
