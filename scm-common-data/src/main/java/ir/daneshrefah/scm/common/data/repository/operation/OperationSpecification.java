package ir.daneshrefah.scm.common.data.repository.operation;

import ir.daneshrefah.scm.common.data.entity.operation.OperationEntity;
import ir.daneshrefah.scm.common.dto.operation.OperationFilterRequest;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class OperationSpecification {
    public static Specification<OperationEntity> toSpecification(OperationFilterRequest request) {
        List<Predicate> predicates = new ArrayList<>();
        return (root, query, builder) -> {
            if (StringUtils.isNotBlank(request.getName())) {
                predicates.add(builder.like(root.get("name"), getLikeQueryString(request.getName())));
            }
            if (StringUtils.isNotBlank(request.getTitle())) {
                predicates.add(builder.like(root.get("title"), getLikeQueryString(request.getTitle())));
            }
            if (StringUtils.isNotBlank(request.getPath())) {
                predicates.add(builder.like(root.get("path"), getLikeQueryString(request.getPath())));
            }
            if (null != request.getActive()) {
                predicates.add(builder.equal(root.get("active"), request.getActive()));
            }
            if (null != request.getType()) {
                predicates.add(builder.like(root.get("type"), request.getType().name()));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static String getLikeQueryString(String input) {
        return "%" + input + "%";
    }
}