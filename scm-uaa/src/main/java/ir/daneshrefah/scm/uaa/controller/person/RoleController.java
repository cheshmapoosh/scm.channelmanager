package ir.daneshrefah.scm.uaa.controller.person;

import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.uaa.domain.person.Role;
import ir.daneshrefah.scm.uaa.service.person.RoleFindRequest;
import ir.daneshrefah.scm.uaa.service.person.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-14
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/role")
public class RoleController {

    private final RoleService roleService;

    @PostMapping("/paged")
    public PagedResponseData<Role> findPagedRoleList(@RequestBody(required = false) RoleFindRequest request) {
        return roleService.findPagedRoleList(request);
    }

}
