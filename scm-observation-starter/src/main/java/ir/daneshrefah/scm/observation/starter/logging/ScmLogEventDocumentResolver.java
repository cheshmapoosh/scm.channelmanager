package ir.daneshrefah.scm.observation.starter.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;

import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

final class ScmLogEventDocumentResolver {
    private static final ScmLogDocumentFactory DOCUMENT_FACTORY = new ScmLogDocumentFactory();
    private static final ReferenceQueue<ILoggingEvent> STALE_EVENTS = new ReferenceQueue<>();
    private static final ConcurrentMap<EventReference, CompletableFuture<Resolution>> RESOLUTIONS =
            new ConcurrentHashMap<>();

    private ScmLogEventDocumentResolver() {
    }

    static Map<String, Object> resolve(ILoggingEvent event) throws IOException {
        if (event == null) {
            return Map.of();
        }

        removeStaleEvents();
        EventReference eventReference = new EventReference(event, STALE_EVENTS);
        CompletableFuture<Resolution> pending = new CompletableFuture<>();
        CompletableFuture<Resolution> existing = RESOLUTIONS.putIfAbsent(eventReference, pending);
        if (existing == null) {
            try {
                pending.complete(Resolution.success(DOCUMENT_FACTORY.create(event)));
            } catch (Throwable failure) {
                pending.complete(Resolution.failure(failure));
                if (failure instanceof Error error) {
                    throw error;
                }
            }
            return pending.join().documentOrThrow();
        }
        return existing.join().documentOrThrow();
    }

    private static void removeStaleEvents() {
        EventReference staleEvent;
        while ((staleEvent = (EventReference) STALE_EVENTS.poll()) != null) {
            RESOLUTIONS.remove(staleEvent);
        }
    }

    private static final class EventReference extends WeakReference<ILoggingEvent> {
        private final int identityHashCode;

        private EventReference(ILoggingEvent event, ReferenceQueue<ILoggingEvent> referenceQueue) {
            super(event, referenceQueue);
            this.identityHashCode = System.identityHashCode(event);
        }

        @Override
        public int hashCode() {
            return identityHashCode;
        }

        @Override
        public boolean equals(Object candidate) {
            if (this == candidate) {
                return true;
            }
            if (!(candidate instanceof EventReference other)) {
                return false;
            }
            ILoggingEvent event = get();
            return event != null && event == other.get();
        }
    }

    private record Resolution(Map<String, Object> document, String failureType) {
        private static Resolution success(Map<String, Object> document) {
            Map<String, Object> immutableDocument = document == null || document.isEmpty()
                    ? Map.of()
                    : Collections.unmodifiableMap(new LinkedHashMap<>(document));
            return new Resolution(immutableDocument, null);
        }

        private static Resolution failure(Throwable failure) {
            String type = failure == null ? "unknown" : failure.getClass().getName();
            return new Resolution(Map.of(), type);
        }

        private Map<String, Object> documentOrThrow() throws IOException {
            if (failureType != null) {
                throw new IOException("SCM LOG document resolution failed: " + failureType);
            }
            return document;
        }
    }
}
