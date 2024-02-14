package ir.daneshrefah.scm.uaa.repository.authentication;

import ir.daneshrefah.scm.uaa.service.person.RoleFindRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-14
 */
public class RoleSpecs {

    public static Specification<RoleEntity> toSpecification(RoleFindRequest request) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (null != request.getCode()) {
                predicates.add(builder.equal(root.get("code"), request.getCode()));
            }
            if (null != request.getSystemRole()) {
                predicates.add(builder.equal(root.get("systemRole"), request.getSystemRole()));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
