package ir.daneshrefah.scm.common.model;

import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class ScmResponse implements Serializable {
    private MessageStatus status;
    private Object result;
    private List<Error> errors;
}
