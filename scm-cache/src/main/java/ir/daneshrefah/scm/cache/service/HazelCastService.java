package ir.daneshrefah.scm.cache.service;

import ir.daneshrefah.scm.cache.client.CacheTemplate;

import java.util.List;

public interface HazelCastService extends CacheTemplate {

    List<String> getMapList();

    Object getMapData(String map);
}
