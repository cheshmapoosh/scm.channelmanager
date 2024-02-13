package ir.daneshrefah.scm.uaa.service.person;

import ir.daneshrefah.scm.common.data.service.person.PersonFindRequest;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;

import java.util.List;

public interface CIFService {

    public List<GeneralPerson> findPersonInfo(PersonFindRequest request);

}
