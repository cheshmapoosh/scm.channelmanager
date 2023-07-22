package ir.daneshrefah.scm.repository;

import ir.daneshrefah.scm.entity.service.ServiceEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends CrudRepository<ServiceEntity, String> {

}
