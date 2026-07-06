package ir.daneshrefah.scm.provider.task.repository;

import ir.daneshrefah.scm.provider.task.entity.ProcessInstanceEntity;
import ir.daneshrefah.scm.provider.task.model.ProcessInstanceFilterRequest;
import ir.daneshrefah.scm.utils.date.DateUtils;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProcessInstanceSpecs {

    public static Specification<ProcessInstanceEntity> toSpecification(ProcessInstanceFilterRequest request) {
        return (root, query, builder) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();
            if (request.isReport()) {
                Join<Object, Object> tasks = root.join("tasks");
                predicates.add(builder.or(
                        builder.equal(root.get("createBy"),request.getUserId()),
                        builder.equal(root.get("confirmUserId"),request.getUserId()),
                        builder.equal(tasks.get("userId"), request.getUserId())
                ));
            }
            if (null != request.getAccountNo()) {
                predicates.add(builder.equal(root.get("accountNo"), request.getAccountNo()));
            }
            if (null != request.getServiceCode()) {
                predicates.add(builder.equal(root.get("serviceCode"), request.getServiceCode()));
            }
            if (Objects.nonNull(request.getStatus())) {
                predicates.add(builder.equal(root.get("processStatus"), request.getStatus()));
            }
            if (Objects.nonNull(request.getProcessCode())) {
                predicates.add(builder.equal(root.get("processCode"), request.getProcessCode()));
            }
            if (Objects.nonNull(request.getFromDate())) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("createAt"), DateUtils.DateConverter.convertToDate(new Timestamp(request.getFromDate()))));
            }
            if (Objects.nonNull(request.getToDate())) {
                predicates.add(builder.lessThanOrEqualTo(root.get("createAt"), DateUtils.DateConverter.convertToDate(new Timestamp(request.getToDate()))));
            }
            if (Objects.nonNull(request.getConfirmUserId())) {
                predicates.add(builder.equal(root.get("confirmUserId"), request.getConfirmUserId()));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
