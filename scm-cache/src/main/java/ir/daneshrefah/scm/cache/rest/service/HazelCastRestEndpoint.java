package ir.daneshrefah.scm.cache.rest.service;

import ir.daneshrefah.scm.cache.client.CacheTemplate;

import java.util.List;

public interface HazelCastRestEndpoint extends CacheTemplate {

    List<String> getMapList();

    Object getMapData(String map);
}
