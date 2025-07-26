package ir.daneshrefah.scm.common.data.repository.logging;

import ir.daneshrefah.scm.common.data.entity.logging.LogTraceEntity;
import ir.daneshrefah.scm.common.model.logging.LogTraceRequest;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TraceLogSpec {

    public static Specification<LogTraceEntity> toSpecification(LogTraceRequest request) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.equal(root.get("logPrimaryKey").get("rowNo"), 0));

            if (StringUtils.isNotBlank(request.getTerminalCode())) {
                predicates.add(builder.equal(root.get("terminalCode"), request.getTerminalCode()));
            }
            if (StringUtils.isNotBlank(request.getCorrelationId())) {
                predicates.add(builder.equal(root.get("correlationId"), request.getCorrelationId()));
            }
            if (StringUtils.isNotBlank(request.getServiceCode())) {
                predicates.add(builder.equal(root.get("serviceCode"), request.getServiceCode()));
            }
            if (StringUtils.isNotBlank(request.getNickname())) {
                predicates.add(builder.equal(root.get("nickname"), request.getNickname()));
            }
            if (StringUtils.isNotBlank(request.getUsername())) {
                predicates.add(builder.equal(root.get("username"), request.getUsername()));
            }
            if (StringUtils.isNotBlank(request.getDelegatorUsername())) {
                predicates.add(builder.equal(root.get("delegatorUsername"), request.getDelegatorUsername()));
            }
            if (StringUtils.isNotBlank(request.getDelegatorNickname())) {
                predicates.add(builder.equal(root.get("delegatorNickname"), request.getDelegatorNickname()));
            }
            if (Objects.nonNull(request.getStartTime())) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("startTime"), request.getStartTime()));
            }
            if (StringUtils.isNotBlank(request.getAccountNo())) {
                predicates.add(builder.equal(root.get("accountNo"), request.getAccountNo()));
            }
            if (StringUtils.isNotBlank(request.getCardNo())) {
                predicates.add(builder.equal(root.get("cardNo"), request.getCardNo()));
            }
            if (Objects.nonNull(request.getStatusCode())) {
                predicates.add(builder.equal(root.get("statusCode"), request.getStatusCode()));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
