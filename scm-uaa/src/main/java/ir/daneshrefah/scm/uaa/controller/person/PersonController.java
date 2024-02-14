package ir.daneshrefah.scm.uaa.controller.person;

import ir.daneshrefah.scm.common.data.service.person.PersonFindRequest;
import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.uaa.service.person.UPersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-12
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/person")
public class PersonController {

    private final UPersonService personService;

    @PostMapping("/paged")
    public PagedResponseData<GeneralPerson> findPagedPersonList(@RequestBody(required = false) PersonFindRequest request) {
        return personService.findPagedPersonList(request);
    }

    @PostMapping("/cif")
    public List<GeneralPerson> findCIFPersonInfo(@RequestBody PersonFindRequest request) {
        return personService.findCIFPersonInfo(request);
    }

    @PostMapping("/add")
    public GeneralPerson addPersonInfoFromCIF(@RequestBody PersonFindRequest request) {
        return personService.addPersonInfoFromCIF(request);
    }

    @PostMapping("/update")
    public GeneralPerson updatePersonInfoFromCIF(@RequestBody PersonFindRequest request) {
        return personService.updatePersonInfoFromCIF(request);
    }

}
