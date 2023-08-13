package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.core.entity.authority.AuthorityEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorityRepository extends CrudRepository<AuthorityEntity, String> {

    @Query("SELECT e FROM AuthorityEntity e WHERE TYPE(e) IN :entityTypes")
    List<AuthorityEntity> findByDiscriminators(@Param("entityTypes") List<Class<? extends AuthorityEntity>> entityTypes);

}
