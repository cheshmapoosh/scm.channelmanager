package ir.daneshrefah.scm.common.log.repository.transaction;

import ir.daneshrefah.scm.common.log.entity.transaction.TransactionLogEntity;
import ir.daneshrefah.scm.common.log.model.TransactionLogRequest;
import ir.daneshrefah.scm.utils.string.ArchiveUtils;
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
            if (Objects.nonNull(request.getAccountNo())) {
                predicates.add(builder.equal(root.get("accountNo"), request.getAccountNo()));
            }
            if (Objects.nonNull(request.getMessageSequenceId())) {
                predicates.add(builder.equal(root.get("messageSequenceId"), request.getChannelId()));
            }
            if (Objects.nonNull(request.getChannelId())) {
                predicates.add(builder.equal(root.get("channelId"), request.getChannelId()));
            }
            Long archiveNo;
            if (Objects.nonNull(request.getLogTime())) {
                archiveNo = ArchiveUtils.calculateOneMonthArchiveNo(new Date(request.getLogTime() * 1000));
            } else {
                archiveNo = ArchiveUtils.calculateOneMonthArchiveNo(new Date());
            }
            predicates.add(builder.equal(root.get("archiveNo"), archiveNo));
            if (Objects.nonNull(request.getTransactionLogId())) {
                predicates.add(builder.equal(root.get("transactionLogId"), request.getTransactionLogId()));
            } else if (Objects.nonNull(request.getNextId())) {
                predicates.add(builder.gt(root.get("transactionLogId"), request.getNextId()));
            } else if (Objects.nonNull(request.getPreviousId())) {
                predicates.add(builder.lessThan(root.get("transactionLogId"), request.getPreviousId()));
            }
            query.orderBy(builder.desc(root.get("transactionLogId")));
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
