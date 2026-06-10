package ir.daneshrefah.scm.cache.repository;

import ir.daneshrefah.scm.cache.domain.config.InstanceConfigEntity;

import java.util.List;

public interface InstanceCacheConfigRepository {

    List<InstanceConfigEntity> findAll();
}