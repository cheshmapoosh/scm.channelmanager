package ir.daneshrefah.scm.uaa.repository.activation;

import ir.daneshrefah.scm.uaa.repository.activation.domain.LoginMessageEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoginMessageRepository extends PagingAndSortingRepository<LoginMessageEntity,Long> {
    default List<LoginMessageEntity> doFindAll(Sort sort){
        return (List<LoginMessageEntity>) findAll(sort);
    };

    default void evictLoginMessage() {
    }


}
