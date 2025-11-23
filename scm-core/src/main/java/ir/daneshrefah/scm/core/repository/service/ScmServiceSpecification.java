package ir.daneshrefah.scm.core.repository.service;

import ir.daneshrefah.scm.common.data.entity.asset.ServiceEntity;
import ir.daneshrefah.scm.common.dto.service.EbServiceFilterRequest;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ScmServiceSpecification {

    public static Specification<ServiceEntity> toSpecification(EbServiceFilterRequest request) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (request.getPublish() != null) {
                predicates.add(builder.equal(root.get("publish"), request.getPublish()));
            }
            if (StringUtils.isNotBlank(request.getAbbreviation())) {
                predicates.add(builder.equal(root.get("abbreviation"), request.getAbbreviation()));
            }
            if (StringUtils.isNotBlank(request.getName())) {
                predicates.add(builder.like(root.get("name"), getLikeQueryString(request.getName())));
            }
            if (request.getRoutingStrategy() != null) {
                predicates.add(builder.equal(root.get("routingStrategy"), request.getRoutingStrategy().name()));
            }
            if (StringUtils.isNotBlank(request.getCode())) {
                predicates.add(builder.equal(root.get("code"), request.getCode()));
            }
            if (request.getFinancial() != null) {
                predicates.add(builder.equal(root.get("financial"), request.getFinancial()));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static String getLikeQueryString(String input) {
        return "%" + input + "%";
    }
}
