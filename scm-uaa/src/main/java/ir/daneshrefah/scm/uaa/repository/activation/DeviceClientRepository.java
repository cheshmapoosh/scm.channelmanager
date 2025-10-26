package ir.daneshrefah.scm.uaa.repository.activation;

import ir.daneshrefah.scm.uaa.repository.activation.domain.DeviceClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceClientRepository extends JpaRepository<DeviceClientEntity,Long> {

    Optional<DeviceClientEntity> findByAppVersion(String appVersion);
}
