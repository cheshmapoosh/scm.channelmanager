package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.core.entity.person.CardEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<CardEntity, Long> {

    @EntityGraph(attributePaths = {"cardType"})
    @Query("SELECT c FROM CardEntity c WHERE c.membership.person.id = :personId")
    List<CardEntity> findAllByPersonId(@Param("personId") Integer personId);

    @EntityGraph(attributePaths = {"cardType"})
    @Query("SELECT c FROM CardEntity c WHERE c.membership.person.username = :username")
    List<CardEntity> findAllByPersonUsername(@Param("username") String username);
}
