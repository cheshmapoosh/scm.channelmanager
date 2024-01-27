package ir.daneshrefah.scm.common.data.entity.person;

import ir.daneshrefah.scm.common.data.converter.NationalityConverter;
import ir.daneshrefah.scm.common.data.entity.AbstractEntity;
import ir.daneshrefah.scm.common.data.type.Nationality;
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
    @Column(name = "NATIONALITY_CODE")
    @Convert(converter = NationalityConverter.class)
    private Nationality nationality;

}
