package ir.daneshrefah.scm.observation.starter.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import ir.daneshrefah.scm.observation.starter.ObservationEventSignal;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

public class DefaultLogbackObservationEventPublisher implements LogbackObservationEventPublisher {
    @Override
    public void publish(ObservationEventSignal signal, Class<?> sourceClass, String payload) {
        if (!(LoggerFactory.getILoggerFactory() instanceof LoggerContext loggerContext)) {
            throw new IllegalStateException("SCM observation TRACE/AUDIT output requires Logback as the active logging backend.");
        }
        Class<?> safeSourceClass = sourceClass == null ? DefaultLogbackObservationEventPublisher.class : sourceClass;
        Logger logger = loggerContext.getLogger(safeSourceClass.getName());
        LoggingEvent event = new LoggingEvent();
        event.setLoggerContext(loggerContext);
        event.setLoggerName(logger.getName());
        event.setLevel(Level.INFO);
        event.setMessage(payload == null ? "" : payload);
        event.setThreadName(Thread.currentThread().getName());
        event.setTimeStamp(System.currentTimeMillis());
        event.addMarker(marker(signal));
        event.setCallerData(callerData(safeSourceClass));
        logger.callAppenders(event);
    }

    private Marker marker(ObservationEventSignal signal) {
        return switch (signal) {
            case TRACE -> ScmObservationMarkers.TRACE;
            case AUDIT -> ScmObservationMarkers.AUDIT;
        };
    }

    private StackTraceElement[] callerData(Class<?> sourceClass) {
        return new StackTraceElement[]{
                new StackTraceElement(sourceClass.getName(), "observation", null, -1)
        };
    }
}
