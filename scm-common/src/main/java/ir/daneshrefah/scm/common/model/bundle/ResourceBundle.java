package ir.daneshrefah.scm.common.model.bundle;

import ir.daneshrefah.scm.common.AbstractAuditableModel;
import lombok.Getter;
import lombok.Setter;

import java.util.Locale;

@Getter
@Setter
public class ResourceBundle extends AbstractAuditableModel<Long> {
    private Long id;
    private Locale locale;
    private String key;
    private String value;
}
