package ir.daneshrefah.scm.cache.client.connector;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import ir.daneshrefah.scm.cache.client.connector.exception.QueueOperationException;
import ir.daneshrefah.scm.cache.client.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
public class QueueTemplateImpl implements QueueTemplate {

    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.registerModule(new JavaTimeModule());
    }

    private final HazelcastInstance hazelcastInstance;

    @Override
    public <T> void push(Message<T> message, String queueName) {
        IQueue<String> queue = hazelcastInstance.getQueue(queueName);
        try {
            queue.put(MAPPER.writeValueAsString(message));
            log.debug("Queue message pushed: queue='{}'", queueName);
        } catch (Exception exception) {
            log.error("Could not push queue message: queue='{}'", queueName, exception);
            throw new QueueOperationException("Could not push message to queue '" + queueName + "'", exception);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<Message<T>> pop(String queueName) {
        IQueue<String> queue = hazelcastInstance.getQueue(queueName);
        try {
            String message = queue.poll(10, TimeUnit.SECONDS);
            if (Objects.nonNull(message)) {
                Message<T> msg = MAPPER.readValue(message, Message.class);
                log.debug("Queue message popped: queue='{}'", queueName);
                return Optional.ofNullable(msg);
            } else {
                return Optional.empty();
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.warn("Queue pop interrupted: queue='{}'", queueName);
            return Optional.empty();
        } catch (Exception exception) {
            log.error("Could not pop queue message: queue='{}'", queueName, exception);
            throw new QueueOperationException("Could not pop message from queue '" + queueName + "'", exception);
        }
    }

    @Override
    public boolean hasAnyMessages(String queueName) {
        IQueue<String> queue = hazelcastInstance.getQueue(queueName);
        boolean hasAny = !queue.isEmpty();
        log.debug("Queue hasAnyMessages: queue='{}', hasAny={}", queueName, hasAny);
        return hasAny;
    }
}
