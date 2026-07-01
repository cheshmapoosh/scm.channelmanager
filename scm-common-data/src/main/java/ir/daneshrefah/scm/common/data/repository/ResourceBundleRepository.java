package ir.daneshrefah.scm.common.data.repository;

import ir.daneshrefah.scm.common.data.entity.bundle.ResourceBundleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Locale;
import java.util.Optional;

@Repository
public interface ResourceBundleRepository extends JpaRepository<ResourceBundleEntity,Long> {

    Optional<ResourceBundleEntity> findByLocaleAndKey(Locale locale,String key);

}
