package ir.daneshrefah.scm.task.model;

import ir.daneshrefah.scm.common.model.person.PersonType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserModel {
    private String nationalId;
    private PersonType personType;
    private String subOrganization;
    private String customerId;
}