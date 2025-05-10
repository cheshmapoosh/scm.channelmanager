package ir.daneshrefah.scm.common.model.person;

import ir.daneshrefah.scm.common.AuditableModel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
@Getter
@Setter
public abstract class GeneralPerson extends AuditableModel<Long> {

    private String username;
    private Nationality nationality;
    private LocalDate registerIssueDate;
    private PersonStatus status;
    private String branchCode;
    private String phone1;
    private String phone2;
    private String mobile1;
    private String mobile2;
    private String mobile3;
    private String email;
    private String fax;
    private String address1;
    private String address2;
    private String address3;
    private String address4;
    private String postalCode1;
    private String postalCode2;
    private String shahabCode;
    private String issuePlace;

    public abstract String getTitle();

    public abstract PersonType getPersonType();

}
