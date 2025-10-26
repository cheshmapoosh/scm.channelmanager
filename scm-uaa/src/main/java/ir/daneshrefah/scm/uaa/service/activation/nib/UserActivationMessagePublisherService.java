package ir.daneshrefah.scm.uaa.service.activation.nib;

public interface UserActivationMessagePublisherService {
    void publish(String username, String fromTerminal);
}
