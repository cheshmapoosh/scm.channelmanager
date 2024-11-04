package ir.daneshrefah.scm.uaa.service.person;

import ir.daneshrefah.scm.common.dto.membership.PersonFindRequest;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;

import java.util.List;

public interface CIFService {

    List<GeneralPerson> findPersonInfo(PersonFindRequest request);

}
