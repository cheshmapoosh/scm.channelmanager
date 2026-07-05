package ir.daneshrefah.scm.common.event;

public final class NoopScmEventPublisher implements ScmEventPublisher {
    public static final NoopScmEventPublisher INSTANCE = new NoopScmEventPublisher();

    private NoopScmEventPublisher() {
    }

    @Override
    public void publish(ScmEvent event) {
        // Intentionally empty for non-Spring or isolated execution contexts.
    }
}
