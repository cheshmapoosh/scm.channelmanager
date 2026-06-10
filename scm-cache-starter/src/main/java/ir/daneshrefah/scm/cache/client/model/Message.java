package ir.daneshrefah.scm.cache.client.model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
public class Message<T> implements Serializable {
    private final LocalDateTime createdAt = LocalDateTime.now();
    Map<String,String> attributes = new HashMap<>();
    private T payload;
}
