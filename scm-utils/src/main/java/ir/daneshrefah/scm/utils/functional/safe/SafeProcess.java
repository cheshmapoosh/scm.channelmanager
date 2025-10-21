package ir.daneshrefah.scm.utils.functional.safe;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class SafeProcess<T> {

    private T value;
    private Exception exception;

    public static <T> SafeProcess<T> of() {
        return new SafeProcess<>();
    }

    public SafeProcess<T> tryRun(ThrowingRunnable run) {
        try {
            run.run();
        } catch (Exception e) {
            log.error("tryRun failed", e);
            this.exception = e;
        }
        return this;
    }


    public SafeProcess<T> tryGet(ThrowingSupplier<T> sup) {
        try {
            this.value = sup.get();
        } catch (Exception e) {
            log.error("tryGet failed", e);
            this.exception = e;
            this.value = null;
        }
        return this;
    }


    public SafeProcess<T> onFailure(Consumer<Exception> handler) {
        if (exception != null) {
            try {
                handler.accept(exception);
            } catch (Exception e) {
                log.error("onFailure handler failed", e);
                throw new RuntimeException(e);
            }
        }
        return this;
    }

    public SafeProcess<T> recover(Function<Exception, ? extends T> fn) {
        if (exception != null) {
            try {
                this.value = fn.apply(exception);
                this.exception = null;
            } catch (Exception e) {
                log.error("recover failed", e);
                this.exception = e;
                this.value = null;
            }
        }
        return this;
    }

    public SafeProcess<T> doFinally(Runnable r) {
        try {
            r.run();
        } catch (Exception e) {
            log.error("finally failed", e);
        }
        return this;
    }

    public boolean isSuccess() {
        return exception == null;
    }

    public boolean isFailure() {
        return exception != null;
    }

    public Optional<Exception> getException() {
        return Optional.ofNullable(exception);
    }

    public Optional<T> toOptional() {
        return Optional.ofNullable(value);
    }

    public T orElse(T other) {
        return value != null ? value : other;
    }

    public T orElseGet(Supplier<? extends T> other) {
        return value != null ? value : other.get();
    }

    public T orElseThrow() {
        if (exception != null) throw new RuntimeException(exception);
        return value;
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    @FunctionalInterface
    public interface ThrowingSupplier<R> {
        R get() throws Exception;
    }
}
