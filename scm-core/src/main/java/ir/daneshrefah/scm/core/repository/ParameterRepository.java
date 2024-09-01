package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.core.entity.service.parameter.ParameterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParameterRepository extends JpaRepository<ParameterEntity,Long> {

    List<ParameterEntity> findAllByParent(ParameterEntity parent);

}
