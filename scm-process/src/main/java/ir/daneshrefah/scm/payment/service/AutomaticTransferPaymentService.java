package ir.daneshrefah.scm.payment.service;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Service;
@Service
public class AutomaticTransferPaymentService implements JavaDelegate {
    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        System.out.println("Automatic Transfer payment");
        System.out.println("ir.daneshrefah.scm.process.service.AutomaticTransferPaymentService");
    }
}
