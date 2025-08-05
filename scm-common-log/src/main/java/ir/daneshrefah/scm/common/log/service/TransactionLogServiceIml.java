package ir.daneshrefah.scm.common.log.service;

import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.log.entity.transaction.TransactionLogEntity;
//import ir.daneshrefah.scm.common.log.entity.transaction.TransactionLogId;
import ir.daneshrefah.scm.common.log.mapper.TransactionLogMapper;
import ir.daneshrefah.scm.common.log.model.TransactionLogRequest;
import ir.daneshrefah.scm.common.log.model.TransactionLogResponse;
import ir.daneshrefah.scm.common.log.model.TransactionPayloadRequest;
import ir.daneshrefah.scm.common.log.repository.transaction.TransactionLogRepository;
import ir.daneshrefah.scm.common.log.repository.transaction.TransactionLogSpec;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class TransactionLogServiceIml implements TransactionLogService {

    private final TransactionLogRepository transactionLogRepository;

    @PersistenceContext(name = "entityManagerFactory")
    private final EntityManager entityManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveAll(List<TransactionLogEntity> transactionLogs) {
        transactionLogRepository.saveAll(transactionLogs);
    }

    @Override
    public PagedResponseData<TransactionLogResponse> findAll(TransactionLogRequest request) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        Specification<TransactionLogEntity> specification = TransactionLogSpec.toSpecification(request);
        CriteriaQuery<TransactionLogEntity> cq = cb.createQuery(TransactionLogEntity.class);
        Root<TransactionLogEntity> root = cq.from(TransactionLogEntity.class);
        cq.where(specification.toPredicate(root, cq, cb));
        List<TransactionLogEntity> resultList = entityManager.createQuery(cq).setMaxResults(10).getResultList();
        TransactionLogMapper instance = TransactionLogMapper.INSTANCE;
        List<TransactionLogResponse> logTraceResponseList = instance.toModelList(resultList);
        return new PagedResponseData<>(request, logTraceResponseList);
    }

    @Override
    public TransactionLogResponse getDetails(TransactionPayloadRequest request) {
        ValidationUtils.checkNull(request.getTransactionLogId(), () -> new MissingRequiredInputException("transactionLogId"));
        ValidationUtils.checkNull(request.getArchiveNo(), () -> new MissingRequiredInputException("archiveNo"));
        return transactionLogRepository.findById(request.getTransactionLogId()).map(transactionLogEntity -> {
            TransactionLogMapper instance = TransactionLogMapper.INSTANCE;
            return instance.toModel(transactionLogEntity);
        }).orElse(new TransactionLogResponse());
    }
}
