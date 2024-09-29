package ir.daneshrefah.scm.task.repository;

import ir.daneshrefah.scm.task.entity.ProcessInstanceEntity;
import ir.daneshrefah.scm.task.model.ProcessInstanceFilterRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ProcessInstanceSpecs {

    public static Specification<ProcessInstanceEntity> toSpecification(ProcessInstanceFilterRequest request) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (null != request.getAccountNo()) {
                predicates.add(builder.equal(root.get("accountNo"), request.getAccountNo()));
            }
            if (null != request.getServiceCode()) {
                predicates.add(builder.equal(root.get("serviceCode"), request.getServiceCode()));
            }
            if (Objects.nonNull(request.getStatus())) {
                predicates.add(builder.equal(root.get("status"), request.getStatus()));
            }
            if (Objects.nonNull(request.getProcessCode())) {
                predicates.add(builder.equal(root.get("processStatus"), request.getProcessCode()));
            }
            if (Objects.nonNull(request.getFromDate())) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("createAt"), new Date(TimeUnit.SECONDS.toMillis(request.getFromDate()))));
            }
            if (Objects.nonNull(request.getToDate())) {
                predicates.add(builder.lessThanOrEqualTo(root.get("createAt"), new Date(TimeUnit.SECONDS.toMillis(request.getToDate()))));
            }
            if (Objects.nonNull(request.getConfirmUserId())) {
                predicates.add(builder.equal(root.get("confirmUserId"), request.getConfirmUserId()));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
