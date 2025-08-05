package ir.daneshrefah.scm.common.log.repository.transaction;

import ir.daneshrefah.scm.common.log.entity.transaction.TransactionLogEntity;
import ir.daneshrefah.scm.common.log.model.TransactionLogRequest;
import ir.daneshrefah.scm.utils.date.DateUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class TransactionLogSpec {

    public static Specification<TransactionLogEntity> toSpecification(TransactionLogRequest request) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            boolean hasOrder = true;
            if (Objects.nonNull(request.getChannelId())) {
                predicates.add(builder.equal(root.get("channelId"), request.getChannelId()));
            }
            if (Objects.nonNull(request.getLogTime())) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("logTime"), DateUtils.DateConverter.convertToLocalDateTime(new Date(request.getLogTime() * 1000))));
                hasOrder = false;
            }
            if (Objects.nonNull(request.getTransactionLogId())) {
                predicates.add(builder.equal(root.get("id").get("transactionLogId"), request.getTransactionLogId()));
            } else if (Objects.nonNull(request.getNextId())) {
                predicates.add(builder.gt(root.get("id").get("transactionLogId"), request.getNextId()));
            } else if (Objects.nonNull(request.getPreviousId())) {
                predicates.add(builder.lessThan(root.get("id").get("transactionLogId"), request.getPreviousId()));
            }
//            if (Objects.isNull(request.getArchiveNo())) {
//                Date date;
//                if (Objects.isNull(request.getLogTime())) {
//                    date = new Date();
//                } else {
//                    date = DateUtils.DateConverter.convertToDate(new Timestamp(request.getLogTime()));
//                }
//                Long archiveNo = ArchiveUtils.calculateOneMonthArchiveNo(date);
//                predicates.add(builder.equal(root.get("id").get("archiveNo"), archiveNo));
//            }
            if (hasOrder) {
                query.orderBy(builder.desc(root.get("id").get("transactionLogId")));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
