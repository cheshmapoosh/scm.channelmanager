package ir.daneshrefah.scm.uaa.controller.role;

import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.uaa.domain.role.Role;
import ir.daneshrefah.scm.uaa.service.role.RoleDTO;
import ir.daneshrefah.scm.uaa.service.person.RoleFindRequest;
import ir.daneshrefah.scm.uaa.service.role.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<PagedResponseData<Role>> findPagedRoleList(@RequestBody(required = false) RoleFindRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(roleService.findPagedRoleList(request));
    }

    @PostMapping("/add")
    public ResponseEntity<Role> createNewRole(@RequestBody RoleDTO roleDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(roleService.createRole(roleDTO));
    }

    @DeleteMapping("/{roleCode}")
    public ResponseEntity<Void> deleteRoleByCode(@PathVariable String roleCode) {
        roleService.deleteRole(roleCode);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @PutMapping("/{roleId}")
    public ResponseEntity<Role> editRoleById(@PathVariable Integer roleId , @RequestBody RoleDTO roleDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(roleService.editRoleByRoleId(roleDTO,roleId));
    }

}
