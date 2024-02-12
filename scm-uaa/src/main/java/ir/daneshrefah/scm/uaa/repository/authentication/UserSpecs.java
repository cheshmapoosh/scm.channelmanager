package ir.daneshrefah.scm.uaa.repository.authentication;

import ir.daneshrefah.scm.uaa.controller.user.UserFindRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-12
 */
public class UserSpecs {

    public static Specification<UserEntity> toSpecification(UserFindRequest request) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (null != request.getActive()) {
                predicates.add(builder.equal(root.get("active"), request.getActive()));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
