package ir.daneshrefah.scm.config.repository;

import ir.daneshrefah.scm.config.model.entity.PropertyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<PropertyEntity,String> {

    List<PropertyEntity> findPropertyEntitiesByApplicationIdAndProfileIdAndLabelKey(String applicationId,String profileId,String labelKey);

    List<PropertyEntity> findPropertyEntitiesByProfileIdAndApplicationId(String profileId,String applicationId);

}
