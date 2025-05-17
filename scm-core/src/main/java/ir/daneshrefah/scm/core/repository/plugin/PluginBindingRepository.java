package ir.daneshrefah.scm.core.repository.plugin;

import ir.daneshrefah.scm.common.model.plugin.PluginScope;
import ir.daneshrefah.scm.core.entity.plugin.PluginBindingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PluginBindingRepository extends JpaRepository<PluginBindingEntity, String> {
  PluginBindingEntity findByScopeAndScopeId(PluginScope scope, String scopeId);
}