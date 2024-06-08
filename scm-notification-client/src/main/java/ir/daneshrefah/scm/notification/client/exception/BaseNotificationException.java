package ir.daneshrefah.scm.notification.client.exception;

import ir.daneshrefah.scm.common.error.spec.AbstractBaseException;
import ir.daneshrefah.scm.common.error.spec.ExceptionSourceAware;
import ir.daneshrefah.scm.common.model.notification.NotificationRequest;
import ir.daneshrefah.scm.utils.string.StringUtils;

import java.util.Objects;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-05
 */
public abstract class BaseNotificationException extends AbstractBaseException implements ExceptionSourceAware {

    private final NotificationRequest request;
    public BaseNotificationException(NotificationRequest request, String message, Throwable cause) {
        super(message, cause);
        this.request = request;
    }

    @Override
    public String getSource() {
        return Objects.isNull(request) ? "null" : StringUtils.joinWith(StringUtils.DASH, request.getRecipient(), request.getTemplate());
    }
}
