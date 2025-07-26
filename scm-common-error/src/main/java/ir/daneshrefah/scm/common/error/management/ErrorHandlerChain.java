package ir.daneshrefah.scm.common.error.management;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public abstract class ErrorHandlerChain {
    private static final List<ErrorHandler> sortedErrorHandlers = new ArrayList<>();
    private final List<ErrorHandler> errorHandlers;

    public abstract List<Class<? extends ErrorHandler>> errorHandlerChain();

    public List<ErrorHandler> getOrdersErrorHandlers() {
        if (sortedErrorHandlers.isEmpty()) {
            synchronized (sortedErrorHandlers) {
                if (sortedErrorHandlers.isEmpty()) {
                    errorHandlerChain().forEach(aClass -> {
                        ErrorHandler foundHandler = errorHandlers
                                .stream()
                                .filter(errorHandler -> errorHandler.getClass().isAssignableFrom(aClass))
                                .findFirst().orElseThrow(IllegalArgumentException::new);
                        sortedErrorHandlers.add(foundHandler);
                    });
                }
            }
        }
        return sortedErrorHandlers;
    }
}
