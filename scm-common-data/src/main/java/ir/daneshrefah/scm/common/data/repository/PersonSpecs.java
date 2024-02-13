package ir.daneshrefah.scm.common.data.repository;

import ir.daneshrefah.scm.common.data.entity.person.GeneralPersonEntity;
import ir.daneshrefah.scm.common.data.service.person.PersonFindRequest;
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
public class PersonSpecs {

    public static Specification<GeneralPersonEntity> toSpecification(PersonFindRequest request) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (null != request.getPersonType()) {
                predicates.add(builder.equal(root.get("personType"), request.getPersonType()));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
