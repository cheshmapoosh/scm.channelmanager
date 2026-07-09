package ir.daneshrefah.scm.cache.starter.connector;




import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import ir.daneshrefah.scm.cache.starter.connector.exception.QueueOperationException;
import ir.daneshrefah.scm.cache.starter.event.ScmCacheEventSupport;
import ir.daneshrefah.scm.cache.starter.model.Message;
import ir.daneshrefah.scm.common.event.cache.ScmCacheEventType;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
public class QueueTemplateImpl implements QueueTemplate {

    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.registerModule(new JavaTimeModule());
    }

    private final HazelcastInstance hazelcastInstance;
    private final ScmCacheEventSupport cacheEventSupport;

    public QueueTemplateImpl(HazelcastInstance hazelcastInstance) {
        this(hazelcastInstance, null);
    }

    public QueueTemplateImpl(HazelcastInstance hazelcastInstance, ScmCacheEventSupport cacheEventSupport) {
        this.hazelcastInstance = hazelcastInstance;
        this.cacheEventSupport = cacheEventSupport;
    }

    @Override
    public <T> void push(Message<T> message, String queueName) {
        long startedAt = System.nanoTime();
        IQueue<String> queue = hazelcastInstance.getQueue(queueName);
        try {
            queue.put(MAPPER.writeValueAsString(message));
            log.debug("Queue message pushed: queue='{}'", queueName);
            publish(ScmCacheEventType.CACHE_QUEUE_OFFERED, queueName, startedAt, "offered", null, null);
        } catch (Exception exception) {
            publish(ScmCacheEventType.CACHE_QUEUE_ERROR, queueName, startedAt, "failure", null, exception);
            log.error("Could not push queue message: queue='{}'", queueName, exception);
            throw new QueueOperationException("Could not push message to queue '" + queueName + "'", exception);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<Message<T>> pop(String queueName) {
        long startedAt = System.nanoTime();
        IQueue<String> queue = hazelcastInstance.getQueue(queueName);
        try {
            String message = queue.poll(10, TimeUnit.SECONDS);
            if (Objects.nonNull(message)) {
                Message<T> msg = readMessage(message);
                log.debug("Queue message popped: queue='{}'", queueName);
                publish(ScmCacheEventType.CACHE_QUEUE_POLLED, queueName, startedAt, "polled", true, null);
                return Optional.ofNullable(msg);
            } else {
                publish(ScmCacheEventType.CACHE_QUEUE_POLLED, queueName, startedAt, "empty", false, null);
                return Optional.empty();
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            publish(ScmCacheEventType.CACHE_QUEUE_ERROR, queueName, startedAt, "interrupted", null, exception);
            log.warn("Queue pop interrupted: queue='{}'", queueName);
            return Optional.empty();
        } catch (Exception exception) {
            publish(ScmCacheEventType.CACHE_QUEUE_ERROR, queueName, startedAt, "failure", null, exception);
            log.error("Could not pop queue message: queue='{}'", queueName, exception);
            throw new QueueOperationException("Could not pop message from queue '" + queueName + "'", exception);
        }
    }

    @Override
    public <T> Optional<Message<T>> take(String queueName) {
        long startedAt = System.nanoTime();
        IQueue<String> queue = hazelcastInstance.getQueue(queueName);
        try {
            String message = queue.take();
            Message<T> msg = readMessage(message);
            log.debug("Queue message taken: queue='{}'", queueName);
            publish(ScmCacheEventType.CACHE_QUEUE_POLLED, queueName, startedAt, "taken", true, null);
            return Optional.ofNullable(msg);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            publish(ScmCacheEventType.CACHE_QUEUE_ERROR, queueName, startedAt, "interrupted", null, exception);
            log.warn("Queue take interrupted: queue='{}'", queueName);
            return Optional.empty();
        } catch (Exception exception) {
            publish(ScmCacheEventType.CACHE_QUEUE_ERROR, queueName, startedAt, "failure", null, exception);
            log.error("Could not take queue message: queue='{}'", queueName, exception);
            throw new QueueOperationException("Could not take message from queue '" + queueName + "'", exception);
        }
    }

    @Override
    public boolean hasAnyMessages(String queueName) {
        IQueue<String> queue = hazelcastInstance.getQueue(queueName);
        boolean hasAny = !queue.isEmpty();
        log.debug("Queue hasAnyMessages: queue='{}', hasAny={}", queueName, hasAny);
        return hasAny;
    }

    @SuppressWarnings("unchecked")
    private <T> Message<T> readMessage(String message) throws java.io.IOException {
        return MAPPER.readValue(message, Message.class);
    }

    private void publish(ScmCacheEventType type, String queueName, long startedAt, String result, Boolean hit, Throwable error) {
        if (cacheEventSupport != null) {
            cacheEventSupport.queueEvent(type, queueName, startedAt, result, hit, error);
        }
    }
}
