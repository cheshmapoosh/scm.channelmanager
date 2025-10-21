package ir.daneshrefah.scm.common.data.repository.definition;

import ir.daneshrefah.scm.common.data.entity.definition.DefinitionEntity;
import ir.daneshrefah.scm.common.dto.definition.DefinitionFilterRequest;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class DefinitionSpecification {
    public static Specification<DefinitionEntity> toSpecification(DefinitionFilterRequest request) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (request != null && StringUtils.isNotBlank(request.getName())) {
                predicates.add(builder.like(root.get("name"), getLikeQueryString(request.getName())));
            }
            if (request != null && StringUtils.isNotBlank(request.getTitle())) {
                predicates.add(builder.like(root.get("title"), getLikeQueryString(request.getTitle())));
            }
            if (request != null && request.getTypes() != null && !request.getTypes().isEmpty()) {
                predicates.add(root.get("type").in(request.getTypes()));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
    private static String getLikeQueryString(String input) {
        return "%" + input + "%";
    }

}
