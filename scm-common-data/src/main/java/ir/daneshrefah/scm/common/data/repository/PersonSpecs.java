package ir.daneshrefah.scm.common.data.repository;

import ir.daneshrefah.scm.common.data.entity.person.GeneralPersonEntity;
import ir.daneshrefah.scm.common.data.service.person.PersonFindRequest;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
            if (null != request.getNationalId()) {
                predicates.add(builder.equal(root.get("nationalCode"), request.getNationalId()));
                predicates.add(builder.equal(root.get("nationalId"), request.getNationalId()));
            }
            if (Objects.nonNull(request.getNationality())){
                predicates.add(builder.equal(root.get("nationality"),request.getNationality()));
            }
            if (Objects.nonNull(request.getSubOrganizationId())){
                predicates.add(builder.equal(root.get("subOrganizationId"),request.getSubOrganizationId()));
            }
            if (Objects.nonNull(request.getActive())){
                predicates.add(builder.equal(root.get("active"),request.getActive()));
            }
            if (Objects.nonNull(request.getBranchCode())){
                predicates.add(builder.like(root.get("branchCode"),getLikeQueryString(request.getBranchCode())));
            }
            if (StringUtils.isNotEmpty(request.getUsername())){
                predicates.add(builder.like(root.get("username"),getLikeQueryString(request.getUsername())));
            }
            if (StringUtils.isNotEmpty(request.getFirstName())){
                predicates.add(builder.like(root.get("firstName"),getLikeQueryString(request.getFirstName())));
            }
            if (StringUtils.isNotEmpty(request.getLastName())){
                predicates.add(builder.like(root.get("lastName"),getLikeQueryString(request.getLastName())));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static String getLikeQueryString(String input){
        return "%"+input+"%";
    }

}
