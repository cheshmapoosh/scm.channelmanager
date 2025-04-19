package ir.daneshrefah.scm.cache.client.connector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import ir.daneshrefah.scm.cache.client.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class QueueTemplateImpl implements QueueTemplate {

    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.registerModule(new JavaTimeModule());
    }

    private final HazelcastInstance hazelcastInstance;

    @Override
    @SneakyThrows
    public <T> void push(Message<T> message, String queueName) {
        IQueue<String> queue = hazelcastInstance.getQueue(queueName);
        queue.put(MAPPER.writeValueAsString(message));
    }

    @Override
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <T> Optional<Message<T>> pop(String queueName) {
        IQueue<String> queue = hazelcastInstance.getQueue(queueName);
        String message = queue.poll(10, TimeUnit.SECONDS);
        if (Objects.nonNull(message)) {
            Message<T> msg = MAPPER.readValue(message, Message.class);
            return Optional.ofNullable(msg);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public boolean hasAnyMessages(String queueName) {
        IQueue<String> queue = hazelcastInstance.getQueue(queueName);
        return !queue.isEmpty();
    }
}
