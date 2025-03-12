package ir.daneshrefah.scm.common.model.person;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientPerson extends GeneralLegalPerson {

    @Override
    public PersonType getPersonType() {
        return PersonType.CLIENT;
    }
}
