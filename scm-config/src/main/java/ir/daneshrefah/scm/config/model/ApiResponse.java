package ir.daneshrefah.scm.config.model;


import java.time.LocalDateTime;

public class ApiResponse<T> {
    private int statusCode;
    private String message;
    private T result;
    private LocalDateTime timestamp;

    public ApiResponse(ResponseStatus status, T result) {
        this.statusCode = status.getCode();
        this.message = status.getMessage();
        this.result = result;
        this.timestamp = LocalDateTime.now();
    }


    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
