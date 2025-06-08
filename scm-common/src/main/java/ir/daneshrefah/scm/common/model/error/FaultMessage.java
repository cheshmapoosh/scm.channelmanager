package ir.daneshrefah.scm.common.model.error;

import lombok.Data;

import java.util.List;
import java.util.Locale;

@Data
public class FaultMessage {
    private String message;
    private String key;
    private List<Object> args;
    private String localized;
    private Locale language;
}
