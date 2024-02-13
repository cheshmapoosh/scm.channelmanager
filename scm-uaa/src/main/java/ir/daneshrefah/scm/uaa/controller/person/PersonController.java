package ir.daneshrefah.scm.uaa.controller.person;

import ir.daneshrefah.scm.common.data.service.person.PersonFindRequest;
import ir.daneshrefah.scm.common.data.service.person.PersonService;
import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    private final PersonService personService;

    @PostMapping("/paged")
    public PagedResponseData<GeneralPerson> findPagedPersonList(@RequestBody(required = false) PersonFindRequest request) {
        return personService.findPagedPersonList(request);
    }

    @PostMapping("/cif")
    public List<GeneralPerson> findCIFPersonInfo(@RequestBody PersonFindRequest request) {
        return personService.findCIFPersonInfo(request);
    }

}
