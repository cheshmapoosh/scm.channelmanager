package ir.daneshrefah.scm.cache.repository;

import ir.daneshrefah.scm.cache.domain.config.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class InstanceCacheConfigRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<InstanceConfigEntity> findAll(){
        List<InstanceConfigEntity> resultList = new ArrayList<>();
        resultList.addAll(entityManager.createQuery("select o from ListCacheConfigEntity o", ListCacheConfigEntity.class).getResultList());
        resultList.addAll(entityManager.createQuery("select o from MapCacheConfigEntity o", MapCacheConfigEntity.class).getResultList());
        resultList.addAll(entityManager.createQuery("select o from MultiMapCacheConfigEntity o", MultiMapCacheConfigEntity.class).getResultList());
        resultList.addAll(entityManager.createQuery("select o from QueueCacheConfigEntity o", QueueCacheConfigEntity.class).getResultList());
        resultList.addAll(entityManager.createQuery("select o from ReplicatedMapCacheConfigEntity o", ReplicatedMapCacheConfigEntity.class).getResultList());
        resultList.addAll(entityManager.createQuery("select o from SetCacheConfigEntity o", SetCacheConfigEntity.class).getResultList());
        resultList.addAll(entityManager.createQuery("select o from TopicCacheConfigEntity o", TopicCacheConfigEntity.class).getResultList());
        return resultList;
    }
}
