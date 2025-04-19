package ir.daneshrefah.scm.uaa.service.activation;

public interface UserActivationMessagePublisherService {
    void publish(String username, String fromTerminal);
}
