package ir.daneshrefah.scm.plugin.api.model.service.external.povider.executor.helper;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageOutput;

@FunctionalInterface
public interface ParameterBodyConsumer {
    Object apply(Message message, Object body, MessageOutput messageOutput);
}
