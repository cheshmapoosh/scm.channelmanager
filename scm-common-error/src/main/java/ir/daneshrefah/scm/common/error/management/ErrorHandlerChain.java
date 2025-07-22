package ir.daneshrefah.scm.common.error.management;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
@RequiredArgsConstructor
public abstract class ErrorHandlerChain {
    private final List<ErrorHandler> errorHandlers;

    public abstract List<Class<? extends ErrorHandler>> errorHandlerChain();

    public List<ErrorHandler> getOrdersErrorHandlers() {
        List<ErrorHandler> orders = new ArrayList<>();
        errorHandlerChain().forEach(aClass -> {
            ErrorHandler foundHandler = errorHandlers
                    .stream()
                    .filter(errorHandler -> errorHandler.getClass().isAssignableFrom(aClass))
                    .findFirst().orElseThrow(IllegalArgumentException::new);
            orders.add(foundHandler);
        });
        return orders;
    }
}
