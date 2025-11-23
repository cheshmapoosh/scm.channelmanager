package ir.daneshrefah.scm.common.data.repository.channel;

import ir.daneshrefah.scm.common.data.entity.gateway.ChannelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChannelRepository extends JpaRepository<ChannelEntity, Short> , JpaSpecificationExecutor<ChannelEntity> {

    @Query("select o from CM_CHANNEL o where o.code = :code and o.parentId is null ")
    Optional<ChannelEntity> findByCode(String code);
}
