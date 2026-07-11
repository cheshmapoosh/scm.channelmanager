package ir.daneshrefah.scm.observation.starter.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.EncoderBase;
import ir.daneshrefah.scm.observation.starter.ObservationStream;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class ScmLogSimpleEncoder extends EncoderBase<ILoggingEvent> {
    private static final byte[] EMPTY = new byte[0];

    private final ScmLogDocumentFactory documentFactory = new ScmLogDocumentFactory();
    private final ObservationSimpleLineFormatter formatter = new ObservationSimpleLineFormatter();

    @Override
    public byte[] headerBytes() {
        return EMPTY;
    }

    @Override
    public byte[] encode(ILoggingEvent event) {
        if (event == null) {
            return EMPTY;
        }
        try {
            return bytes(formatter.format(ObservationStream.LOG, documentFactory.create(event)));
        } catch (IOException | RuntimeException exception) {
            addError("Failed to build an SCM LOG observation document for simple output.", exception);
            return EMPTY;
        }
    }

    @Override
    public byte[] footerBytes() {
        return EMPTY;
    }

    private byte[] bytes(String line) {
        return line.getBytes(StandardCharsets.UTF_8);
    }
}
