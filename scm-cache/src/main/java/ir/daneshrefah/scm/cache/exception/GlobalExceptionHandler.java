package ir.daneshrefah.scm.cache.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Objects;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(HttpServletRequest request, Exception exception) {
        ResponseResult responseResult = createResponseResult(exception);
        if (responseResult.getCode().equals("9999")) {
            return ResponseEntity.internalServerError().body(responseResult);
        }
        return ResponseEntity.status(200).body(responseResult);//TODO change 200 to 400
    }

    private ResponseResult createResponseResult(Exception exception) {
        Throwable throwable = getFinalException(exception);
        ResponseResult responseResult = new ResponseResult();
        if (throwable instanceof DefaultCacheException defaultCacheException) {
            responseResult.setCode(defaultCacheException.getCode());
            responseResult.setMessage(defaultCacheException.getMessage());
        } else {
            responseResult.setCode("9999");
            responseResult.setMessage(throwable.getMessage());
        }
        return responseResult;
    }

    private Throwable getFinalException(Throwable exception) {
        Throwable cause = exception.getCause();
        if (Objects.nonNull(cause)) {
            return getFinalException(cause);
        }
        return exception;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    private static class ResponseResult {
        private String code;
        private String message;
    }
}
