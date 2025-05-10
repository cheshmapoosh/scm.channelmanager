package ir.daneshrefah.scm.common.model.bundle;

import ir.daneshrefah.scm.common.AuditableModel;
import lombok.Getter;
import lombok.Setter;

import java.util.Locale;

@Getter
@Setter
public class ResourceBundle extends AuditableModel<Long> {
    private Long id;
    private Locale locale;
    private String key;
    private String value;
}
