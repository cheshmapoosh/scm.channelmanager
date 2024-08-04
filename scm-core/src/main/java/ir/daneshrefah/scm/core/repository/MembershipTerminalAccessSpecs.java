package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.common.service.MembershipFindRequest;
import ir.daneshrefah.scm.core.entity.asset.MembershipTerminalAccessEntity;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-02
 */
public class MembershipTerminalAccessSpecs {

    public static Specification<MembershipTerminalAccessEntity> toSpecification(MembershipFindRequest request) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (null != request.getAssetType()) {
                predicates.add(builder.equal(root.get("membership").get("assetType"), request.getAssetType()));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
