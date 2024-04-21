package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.core.entity.terminal.ChannelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ChannelRepository extends JpaRepository<ChannelEntity, String> {

    @Transactional
    int deleteByIdAndLastEditDate(String id, LocalDateTime localDateTime);
    Optional<ChannelEntity> findByCode(String code);
}
