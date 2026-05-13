package ir.daneshrefah.scm.cache.client.connector;

import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import ir.daneshrefah.scm.cache.client.connector.exception.QueueOperationException;
import ir.daneshrefah.scm.cache.client.model.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueueTemplateImplTest {

    @Mock
    private HazelcastInstance hazelcastInstance;
    @Mock
    private IQueue<String> queue;

    @AfterEach
    void clearInterruptedFlag() {
        Thread.interrupted();
    }

    @Test
    void pushWrapsQueueErrorsWithTypedException() throws Exception {
        when(hazelcastInstance.<String>getQueue("q1")).thenReturn(queue);
        doThrow(new RuntimeException("boom")).when(queue).put(anyString());

        QueueTemplateImpl queueTemplate = new QueueTemplateImpl(hazelcastInstance);
        Message<String> message = new Message<>();
        message.setPayload("payload");

        assertThrows(QueueOperationException.class, () -> queueTemplate.push(message, "q1"));
    }

    @Test
    void popReturnsEmptyWhenInterrupted() throws Exception {
        when(hazelcastInstance.<String>getQueue("q2")).thenReturn(queue);
        when(queue.poll(10, java.util.concurrent.TimeUnit.SECONDS)).thenThrow(new InterruptedException("interrupted"));

        QueueTemplateImpl queueTemplate = new QueueTemplateImpl(hazelcastInstance);

        Optional<Message<String>> result = queueTemplate.pop("q2");

        assertTrue(result.isEmpty());
        assertTrue(Thread.currentThread().isInterrupted());
    }

    @Test
    void popDeserializesMessageWhenPayloadExists() throws Exception {
        when(hazelcastInstance.<String>getQueue("q3")).thenReturn(queue);
        when(queue.poll(10, java.util.concurrent.TimeUnit.SECONDS))
                .thenReturn("{\"payload\":\"hello\",\"attributes\":{\"k\":\"v\"}}");

        QueueTemplateImpl queueTemplate = new QueueTemplateImpl(hazelcastInstance);

        Optional<Message<String>> result = queueTemplate.pop("q3");

        assertTrue(result.isPresent());
        assertEquals("hello", result.get().getPayload());
        assertEquals("v", result.get().getAttributes().get("k"));
    }
}
