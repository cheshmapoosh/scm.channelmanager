package ir.daneshrefah.scm.common.data.repository.channel;

import ir.daneshrefah.scm.common.data.entity.gateway.ChannelEntity;
import ir.daneshrefah.scm.common.dto.channel.ChannelFindRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChannelSpecification {

    public static Specification<ChannelEntity> toSpecification(ChannelFindRequest request) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (Objects.nonNull(request.getCode())) {
                predicates.add(builder.equal(root.get("code"), request.getCode()));
            }
            if (Objects.nonNull(request.getName())) {
                predicates.add(builder.like(root.get("name"), getLikeQueryString(request.getName())));
            }
            if (Objects.nonNull(request.getTitle())) {
                predicates.add(builder.like(root.get("title"), getLikeQueryString(request.getTitle())));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static String getLikeQueryString(String input) {
        return "%" + input + "%";
    }
}