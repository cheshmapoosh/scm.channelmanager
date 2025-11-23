package ir.daneshrefah.scm.uaa.service.activation.pwa.services.activation;

import ir.daneshrefah.scm.uaa.service.activation.pwa.model.ActivationRequest;
import ir.daneshrefah.scm.uaa.service.activation.pwa.model.ActivationResponse;

public interface PwaActivationService {
    ActivationResponse activationRequest(ActivationRequest request);

    ActivationResponse verificationRequest(ActivationRequest request);

}
