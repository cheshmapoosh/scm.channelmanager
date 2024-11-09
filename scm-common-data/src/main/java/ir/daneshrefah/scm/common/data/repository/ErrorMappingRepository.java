package ir.daneshrefah.scm.common.data.repository;

import ir.daneshrefah.scm.common.data.entity.error.ErrorMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ErrorMappingRepository extends JpaRepository<ErrorMappingEntity, Long> {

    Optional<ErrorMappingEntity> findByErrorMessage(String errorMessage);
    Optional<ErrorMappingEntity> findByProviderId(String providerId);
}
