package ir.daneshrefah.scm.core.repository.plugin;

import ir.daneshrefah.scm.common.model.plugin.PluginScope;
import ir.daneshrefah.scm.core.entity.plugin.PluginBindingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PluginBindingRepository extends JpaRepository<PluginBindingEntity, String> {
  PluginBindingEntity findByScopeAndScopeIdAndActive(PluginScope scope, String scopeId, boolean active);
}