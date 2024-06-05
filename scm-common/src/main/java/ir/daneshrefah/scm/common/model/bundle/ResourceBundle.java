package ir.daneshrefah.scm.common.model.bundle;

import ir.daneshrefah.scm.common.BaseModel;
import lombok.Getter;
import lombok.Setter;

import java.util.Locale;

@Getter
@Setter
public class ResourceBundle extends BaseModel<String> {
    private String id;
    private Locale locale;
    private String key;
    private String value;
}
