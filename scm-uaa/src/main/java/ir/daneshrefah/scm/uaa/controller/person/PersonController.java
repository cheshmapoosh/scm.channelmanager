package ir.daneshrefah.scm.uaa.controller.person;

import ir.daneshrefah.scm.common.dto.membership.PersonFindRequest;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.model.person.DiffGeneralPerson;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.uaa.domain.role.Role;
import ir.daneshrefah.scm.uaa.service.person.UPersonService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@CrossOrigin
public class PersonController {

    private final UPersonService personService;

    @PostMapping("/list")
    public ResponseEntity<PagedResponseData<GeneralPerson>> findPagedPersonList(@RequestBody(required = false) PersonFindRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(personService.findPagedPersonList(request));
    }

    @PostMapping("/local")
    public ResponseEntity<GeneralPerson> findLocalPerson(@RequestBody PersonFindRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(personService.findLocalPerson(request));
    }

    @GetMapping("/find/{personId}")
    public ResponseEntity<GeneralPerson> findGeneralPersonById(@PathVariable("personId") String personId){
        return ResponseEntity.status(HttpStatus.OK).body(personService.findPersonByPersonId(Integer.parseInt(personId)));
    }

    @GetMapping("/find-nickname/{nickname}/{terminalCode}")
    public ResponseEntity<GeneralPerson> findGeneralPersonByNicknameAndTerminalCode(@PathVariable("nickname") String nickname,
                                                                                    @PathVariable("terminalCode") String terminalCode) {
        return ResponseEntity.status(HttpStatus.OK).body(personService.findPersonByNicknameAndTerminalCode(nickname, terminalCode));
    }

    @GetMapping("/{personId}/roles")
    public ResponseEntity<List<Role>> findUserRoleList(@PathVariable("personId") Integer personId) {
        return ResponseEntity.status(HttpStatus.OK).body(personService.findPersonRoleList(personId));
    }

    @PutMapping("/{personId}/roles/{roleId}")
    public ResponseEntity<Role> addPersonRole(@PathVariable("personId") Integer personId, @PathVariable("roleId") Integer roleId) {
        return ResponseEntity.status(HttpStatus.OK).body(personService.addPersonRole(personId, roleId));
    }

    @PostMapping("/cif")
    public ResponseEntity<List<GeneralPerson>> findCIFPersonInfo(@RequestBody PersonFindRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(personService.findCIFPersonInfo(request));
    }

    @PostMapping("/add")
    public ResponseEntity<GeneralPerson> addPersonInfoFromCIF(@Valid @NotNull @RequestBody PersonFindRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(personService.syncPersonByCif(request));
    }

    @GetMapping("/sync/{personId}")
    public ResponseEntity<GeneralPerson> syncPersonInfoFromCIF(@PathVariable("personId") Integer personId) {
        return ResponseEntity.status(HttpStatus.OK).body(personService.syncPersonInfoFromCIF(personId));
    }

    @GetMapping("/diff/{personId}")
    public ResponseEntity<DiffGeneralPerson> diffPersonInfoFromCIFAndLocal(@PathVariable("personId")  Integer personId ) {
        return ResponseEntity.status(HttpStatus.OK).body(personService.diffPersonInfoFromCIFAndLocal(personId));
    }

}
