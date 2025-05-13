package ir.daneshrefah.scm.common.data.repository.gateway;

import ir.daneshrefah.scm.common.data.entity.gateway.CmChannelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CmChannelRepository extends JpaRepository<CmChannelEntity,Integer> {
}
