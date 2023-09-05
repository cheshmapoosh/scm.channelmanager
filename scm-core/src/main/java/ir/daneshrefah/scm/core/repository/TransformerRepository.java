package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.core.entity.transformer.TransformerEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransformerRepository extends CrudRepository<TransformerEntity, String> {

}
