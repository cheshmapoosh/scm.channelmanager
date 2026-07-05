package ir.daneshrefah.scm.web.observation.plugin;

import ir.daneshrefah.scm.common.event.ScmSafeEventAttributes;
import ir.daneshrefah.scm.common.event.plugin.ScmPluginEvent;
import ir.daneshrefah.scm.web.observation.ScmWebObservationEvent;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ScmWebPluginObservationMapper {
    private static final String PLUGIN_FAILED = "plugin.failed";

    public ScmWebObservationEvent map(ScmPluginEvent event) {
        String action = event == null ? "plugin.event" : event.eventType();
        Map<String, Object> attributes = attributes(event);
        return new ScmWebObservationEvent(
                "plugin",
                action,
                outcome(action),
                "SCM plugin event",
                attributes,
                attributes
        );
    }

    private Map<String, Object> attributes(ScmPluginEvent event) {
        Map<String, Object> attributes = event == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(ScmSafeEventAttributes.mutableCopyOf(event.attributes()));
        if (event != null) {
            attributes.put("scm.event.type", event.eventType());
            attributes.put("scm.event.source", event.metadata().source());
            attributes.put("scm.event.occurred_at", event.occurredAt().toString());
        }
        return attributes;
    }

    private String outcome(String action) {
        return PLUGIN_FAILED.equals(action) ? "failure" : "success";
    }
}
