package ir.daneshrefah.scm.uaa.service.person;

import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.uaa.domain.person.Role;
import ir.daneshrefah.scm.uaa.mapper.RoleMapper;
import ir.daneshrefah.scm.uaa.repository.authentication.RoleEntity;
import ir.daneshrefah.scm.uaa.repository.authentication.RoleRepository;
import ir.daneshrefah.scm.uaa.repository.authentication.RoleSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

    public PagedResponseData<Role> findPagedRoleList(RoleFindRequest request) {
        if (null == request) {
            request = new RoleFindRequest();
        }
        Pageable pageable = PageRequest.of(Math.max(request.getPageNo() - 1, 0), request.getPageSize());
        Page<RoleEntity> entities = roleRepository.findAll(RoleSpecs.toSpecification(request), pageable);
        return new PagedResponseData<>(request.getPageNo(), request.getPageSize(), entities.getTotalElements(),
                RoleMapper.INSTANCE.toModels(entities.getContent()));
    }

}
