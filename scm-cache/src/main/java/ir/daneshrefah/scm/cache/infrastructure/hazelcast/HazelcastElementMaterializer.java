package ir.daneshrefah.scm.cache.infrastructure.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import org.springframework.stereotype.Component;

@Component
public class HazelcastElementMaterializer {
    public void materialize(
            HazelcastInstance instance,
            HazelcastInitializationPlan plan
    ) {
        for (HazelcastElementDefinition element : plan.elements()) {
            materialize(instance, element);
        }
    }

    private void materialize(
            HazelcastInstance instance,
            HazelcastElementDefinition element
    ) {
        try {
            switch (element.type()) {
                case MAP -> instance.getMap(element.name());
                case MULTI_MAP -> instance.getMultiMap(element.name());
                case REPLICATED_MAP -> instance.getReplicatedMap(element.name());
                case QUEUE -> instance.getQueue(element.name());
                case TOPIC -> instance.getTopic(element.name());
                case LIST -> instance.getList(element.name());
                case SET -> instance.getSet(element.name());
            }
        } catch (RuntimeException exception) {
            throw new HazelcastElementMaterializationException(
                    element.type(),
                    element.name(),
                    exception
            );
        }
    }
}
