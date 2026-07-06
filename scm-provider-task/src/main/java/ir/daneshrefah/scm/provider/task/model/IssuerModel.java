package ir.daneshrefah.scm.provider.task.model;

import ir.daneshrefah.scm.common.model.person.PersonType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IssuerModel {
    private String nationalId;
    private String firstName;
    private String lastName;
    private PersonType personType;
}