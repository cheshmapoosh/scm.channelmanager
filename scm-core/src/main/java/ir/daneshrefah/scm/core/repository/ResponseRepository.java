package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.core.entity.service.parameter.ResponseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResponseRepository extends JpaRepository<ResponseEntity,String > {
}
