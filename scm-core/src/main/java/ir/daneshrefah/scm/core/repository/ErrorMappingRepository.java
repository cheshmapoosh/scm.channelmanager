package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.core.entity.common.ErrorMappingEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ErrorMappingRepository extends CrudRepository<ErrorMappingEntity, String> {

}
