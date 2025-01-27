package ir.daneshrefah.scm.uaa.repository.authentication.client;

import ir.daneshrefah.scm.common.model.person.PersonType;
import ir.daneshrefah.scm.uaa.repository.authentication.UserEntity;
import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class FindUserByNationalCodeSpecs {

    public static Specification<UserEntity> toSpecification(PersonType personType, String nationalCode, String subOrganizationId, Long terminalId) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (nationalCode != null && !nationalCode.isBlank()) {
                Predicate nationalCodePredicate = builder.or(
                        builder.equal(root.get("person").get("nationalCode"), nationalCode),
                        builder.equal(root.get("person").get("nationalId"), nationalCode)
                );
                predicates.add(nationalCodePredicate);
            }
            if (personType != null) {
                predicates.add(builder.equal(root.get("person").get("personType"), personType));
            }
            if (subOrganizationId != null && !subOrganizationId.isBlank()) {
                predicates.add(builder.equal(root.get("person").get("subOrganizationId"), subOrganizationId));
            }
            if (terminalId != null) {
                predicates.add(builder.equal(root.get("terminalId"), terminalId));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}