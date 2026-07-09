package ir.daneshrefah.scm.cache.starter.connector;

import ir.daneshrefah.scm.cache.starter.model.Message;

import java.util.Optional;

public interface QueueTemplate {

    <T> void push(Message<T> message, String queueName);

    <T> Optional<Message<T>> pop(String queueName);

    boolean hasAnyMessages(String queueName);
}
