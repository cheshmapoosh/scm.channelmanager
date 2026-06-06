package ir.daneshrefah.scm.common.data.repository.card;

import ir.daneshrefah.scm.common.data.entity.cardPrefix.CardPrefixEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CardPrefixRepository extends JpaRepository<CardPrefixEntity, Long>, JpaSpecificationExecutor<CardPrefixEntity> {
}