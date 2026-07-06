package ir.daneshrefah.scm.task.repository;

import ir.daneshrefah.scm.task.constant.TaskStatusEnum;
import ir.daneshrefah.scm.task.entity.TaskEntity;
import ir.daneshrefah.scm.task.model.TaskFilterRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class TaskSpecs {

    public static final String PROCESS_INSTANCE = "processInstance";

    public static Specification<TaskEntity> toSpecification(TaskFilterRequest request) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (null != request.getAccountNo()) {
                predicates.add(builder.equal(root.get(PROCESS_INSTANCE).get("accountNo"), request.getAccountNo()));
            }
            if (null != request.getServiceCode()) {
                predicates.add(builder.equal(root.get(PROCESS_INSTANCE).get("serviceCode"), request.getServiceCode()));
            }
            if (Objects.nonNull(request.getStatus())) {
                if (request.getStatus().equals(TaskStatusEnum.CURRENT)) {
                    predicates.add(builder.or(
                            builder.equal(root.get("taskStatus"), TaskStatusEnum.PENDING),
                            builder.equal(root.get("taskStatus"), TaskStatusEnum.WAITING_FOR_CONFIRM)
                    ));
                } else {
                    predicates.add(builder.equal(root.get("taskStatus"), request.getStatus()));
                }
            }
            if (Objects.nonNull(request.getTransactionType())) {
                predicates.add(builder.equal(root.get(PROCESS_INSTANCE).get("transactionType"), request.getTransactionType()));
            }
            if (Objects.nonNull(request.getFromDate())) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("createAt"), new Date(TimeUnit.SECONDS.toMillis(request.getFromDate()))));
            }
            if (Objects.nonNull(request.getToDate())) {
                predicates.add(builder.lessThanOrEqualTo(root.get("createAt"), new Date(TimeUnit.SECONDS.toMillis(request.getToDate()))));
            }
            if (Objects.nonNull(request.getUserId())) {
                predicates.add(builder.equal(root.get("userId"), request.getUserId()));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
