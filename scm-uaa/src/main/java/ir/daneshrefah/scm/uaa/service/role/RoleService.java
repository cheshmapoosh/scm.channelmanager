package ir.daneshrefah.scm.uaa.service.role;

import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.exception.InputAlreadyExistException;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.uaa.domain.role.Role;
import ir.daneshrefah.scm.uaa.mapper.RoleMapper;
import ir.daneshrefah.scm.uaa.repository.authentication.RoleEntity;
import ir.daneshrefah.scm.uaa.repository.authentication.RoleRepository;
import ir.daneshrefah.scm.uaa.repository.authentication.RoleSpecs;
import ir.daneshrefah.scm.uaa.service.person.RoleFindRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-14
 */
@RequiredArgsConstructor
@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    public PagedResponseData<Role> findPagedRoleList(RoleFindRequest request) {
        if (null == request) {
            request = new RoleFindRequest();
        }
        Pageable pageable = PageRequest.of(Math.max(request.getPageNo() - 1, 0), request.getPageSize());
        Page<RoleEntity> entities = roleRepository.findAll(RoleSpecs.toSpecification(request), pageable);
        return new PagedResponseData<>(request.getPageNo(), request.getPageSize(), entities.getTotalElements(),
                roleMapper.toModels(entities.getContent()));
    }

    public Role createRole(RoleDTO roleDTO) {
        String roleCode = roleDTO.getCode();
        if (null == roleCode) {
            throw new InvalidInputException("Role Code Is Empty");
        } else {
            Optional<RoleEntity> roleEntity = roleRepository.findByCode(roleCode);
            if (roleEntity.isPresent()) {
                throw new InputAlreadyExistException("Role With This Code '" + roleCode + "' Already Exists");
            } else {
                RoleEntity newRole = roleMapper.roleDtoToRoleEntity(roleDTO);
                roleRepository.save(newRole);

                return roleMapper.toModel(newRole);
            }
        }
    }

    public void deleteRole(String roleCode) {
        Optional<RoleEntity> roleEntity = roleRepository.findByCode(roleCode);
        if (roleEntity.isPresent()) {
            roleRepository.deleteRoleEntityByCode(roleCode);
        }
    }

    public Role editRoleByRoleId(RoleDTO roleDTO, Integer roleId) {
        if (null == roleId) {
            throw new MissingRequiredInputException("Role Id Is Empty");
        } else {
            Optional<RoleEntity> existingRole = roleRepository.findById(roleId);
            if (existingRole.isPresent()) {
                roleMapper.updateRoleEntityFromDto(roleDTO,existingRole.get());
                roleRepository.save(existingRole.get());

                return roleMapper.toModel(existingRole.get());
            } else {
                throw new NoMatchRecordFoundException("Role Not Found");
            }
        }
    }

}
