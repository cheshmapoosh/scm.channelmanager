package ir.daneshrefah.scm.task.model;

import ir.daneshrefah.scm.common.model.person.PersonType;
import lombok.Data;

@Data
public class UserModel {
    private String nationalId;
    private PersonType personType;
    private String subOrganization;
}