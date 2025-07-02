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

            if (StringUtils.isNotBlank(request.getChannelCode())) {
                predicates.add(builder.equal(root.get("channelCode"), request.getChannelCode()));
            }
            if (StringUtils.isNotBlank(request.getTerminalCode())) {
                predicates.add(builder.equal(root.get("terminalCode"), request.getTerminalCode()));
            }
            if (StringUtils.isNotBlank(request.getClientId())) {
                predicates.add(builder.equal(root.get("clientId"), request.getClientId()));
            }
            if (StringUtils.isNotBlank(request.getCorrelationId())) {
                predicates.add(builder.equal(root.get("correlationId"), request.getCorrelationId()));
            }
            if (StringUtils.isNotBlank(request.getClientCorrelationId())) {
                builder.equal(root.get("clientCorrelationId"), request.getClientCorrelationId());
            }
            if (StringUtils.isNotBlank(request.getFlowId())) {
                predicates.add(builder.equal(root.get("flowId"), request.getFlowId()));
            }
            if (StringUtils.isNotBlank(request.getMessageId())) {
                predicates.add(builder.equal(root.get("messageId"), request.getMessageId()));
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
            if (StringUtils.isNotBlank(request.getHostAddress())) {
                predicates.add(builder.equal(root.get("hostAddress"), request.getHostAddress()));
            }
            if (Objects.nonNull(request.getStartTime())) {
                predicates.add(builder.equal(root.get("startTime"), request.getStartTime()));
            }
            if (Objects.nonNull(request.getEndTime())) {
                predicates.add(builder.equal(root.get("endTime"), request.getEndTime()));
            }
            if (StringUtils.isNotBlank(request.getExceptionClassName())) {
                predicates.add(builder.equal(root.get("exceptionClassName"), request.getExceptionClassName()));
            }
            if (StringUtils.isNotBlank(request.getEndPoint())) {
                predicates.add(builder.equal(root.get("endPoint"), request.getEndPoint()));
            }
            if (StringUtils.isNotBlank(request.getAmount())) {
                predicates.add(builder.equal(root.get("amount"), request.getAmount()));
            }
            if (StringUtils.isNotBlank(request.getAccountNo())) {
                predicates.add(builder.equal(root.get("accountNo"), request.getAccountNo()));
            }
            if (StringUtils.isNotBlank(request.getCardNo())) {
                predicates.add(builder.equal(root.get("cardNo"), request.getCardNo()));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
