package ir.daneshrefah.scm.common.data.entity.person;

import ir.daneshrefah.scm.common.data.converter.NationalityConverter;
import ir.daneshrefah.scm.common.data.converter.PersonStatusConverter;
import ir.daneshrefah.scm.common.data.converter.PersonTypeConverter;
import ir.daneshrefah.scm.common.data.entity.AbstractEntity;
import ir.daneshrefah.scm.common.model.person.Nationality;
import ir.daneshrefah.scm.common.model.person.PersonStatus;
import ir.daneshrefah.scm.common.model.person.PersonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "userSeq")
    @SequenceGenerator(name = "userSeq",sequenceName = "SQUSER",allocationSize = 1)
    @Column(name = "USER_ID")
    private Integer id;
    @Column(name = "USERNAME", nullable = false)
    private String username;
    @Column(name = "USER_TYPE", insertable = false, updatable = false)
    @Convert(converter = PersonTypeConverter.class)
    private PersonType personType;
    @Column(name = "NATIONALITY_CODE")
    @Convert(converter = NationalityConverter.class)
    private Nationality nationality;
    @Column(name = "ISSUE_DATE")
    private LocalDate registerIssueDate;
    @Column(name = "ISSUE_PLACE")
    private String issuePlace;
    @Column(name = "ACTIVE")
    @Convert(converter = PersonStatusConverter.class)
    private PersonStatus status;
    /**
     * values are in REF.BRANCH table
     * from CIF comes from 'BRANCHCODE'
     * */
    @Column(name = "BRANCH_CODE")
    private String branchCode;
    @Column(name = "phone")
    private String phone1;
    @Transient
    private String phone2;
    @Column(name = "MOBILE")
    private String mobile1;
    @Transient
    private String mobile2;
    @Transient
    private String mobile3;
    private String email;
    @Column(name = "FAX_NUMBER")
    private String fax;
    @Column(name = "ADDRESS")
    private String address1;
    @Transient
    private String address2;
    @Transient
    private String address3;
    @Transient
    private String address4;
    @Column(name = "POSTAL_CODE")
    private String postalCode1;
    @Transient
    private String postalCode2;
    @Transient
    private String shahabCode;
    @Column(name = "ARCHIVE_NO", updatable = false)
    private Long archiveNo;

}
